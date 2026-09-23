import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { firstValueFrom } from 'rxjs';
import { AuthService } from '../../services/auth.service';
import { DemandaService } from '../../services/demanda.service';
import { AnexoService } from '../../services/anexo.service';
import { AutoService } from '../../services/auto.service';
import { RelatorioService } from '../../services/relatorio.service';
import { AutoPreviewComponent } from '../auto-preview/auto-preview.component';
import { RelatorioPreviewComponent } from '../relatorio-preview/relatorio-preview.component';
import { Anexo, AutoFiscalizacao, Demanda, DocumentoPontuacao, RankingFiscal, Relatorio, Usuario } from '../../models/models';

@Component({
  selector: 'app-gestor-dashboard',
  standalone: true,
  imports: [CommonModule, FormsModule, AutoPreviewComponent, RelatorioPreviewComponent],
  templateUrl: './gestor-dashboard.component.html',
  styleUrls: ['./gestor-dashboard.component.css']
})
export class GestorDashboardComponent implements OnInit {
  currentUser: any = null;
  demandas: Demanda[] = [];
  fiscais: Usuario[] = [];
  loading: boolean = true;
  modalAberta: boolean = false;
  salvando: boolean = false;

  novaDemanda: Partial<Demanda> = {
    titulo: '',
    descricao: ''
  };

  modalEdicaoAberta: boolean = false;
  demandaEmEdicao: Demanda | null = null;
  editando: boolean = false;

  fiscalSelecionadoId: string = '';
  anexoNovaDemanda: File | null = null;

  termoBusca: string = '';

  ranking: RankingFiscal[] = [];
  fiscalExpandidoId: string | null = null;
  modalDocumentoAberto: boolean = false;
  carregandoDocumento: boolean = false;
  documentoSelecionado: AutoFiscalizacao | Relatorio | null = null;
  tipoDocumentoSelecionado: 'AUTO' | 'RELATORIO' | null = null;

  constructor(
    private authService: AuthService,
    private demandaService: DemandaService,
    private anexoService: AnexoService,
    private autoService: AutoService,
    private relatorioService: RelatorioService,
    private cdr: ChangeDetectorRef
  ) {}

  get demandasFiltradas(): Demanda[] {
    const termo = this.termoBusca.trim().toLowerCase();
    if (!termo) return this.demandas;
    return this.demandas.filter(d =>
      d.titulo.toLowerCase().includes(termo) ||
      d.descricao.toLowerCase().includes(termo)
    );
  }

  get iniciaisUsuario(): string {
    const nome = this.currentUser?.nome || '';
    return nome
      .split(' ')
      .filter((p: string) => p.length > 0)
      .slice(0, 2)
      .map((p: string) => p[0].toUpperCase())
      .join('');
  }

  ngOnInit(): void {
    this.currentUser = this.authService.currentUser();
    this.carregarDados();
  }

  carregarDados(): void {
    this.loading = true;
    this.demandaService.listarTodas().subscribe({
      next: (res) => {
        this.demandas = res;
        this.loading = false;
        this.cdr.markForCheck();
      },
      error: () => {
        this.loading = false;
        this.cdr.markForCheck();
      }
    });

    this.demandaService.listarFiscais().subscribe({
      next: (res) => {
        this.fiscais = res;
        this.cdr.markForCheck();
      }
    });

    this.demandaService.rankingFiscais().subscribe({
      next: (res) => {
        this.ranking = res;
        this.cdr.markForCheck();
      }
    });
  }

  contarStatus(status: string): number {
    return this.demandas.filter(d => d.status === status).length;
  }

  abrirModalNovaDemanda(): void {
    this.novaDemanda = { titulo: '', descricao: '' };
    this.fiscalSelecionadoId = '';
    this.anexoNovaDemanda = null;
    this.modalAberta = true;
  }

  fecharModalNovaDemanda(): void {
    this.modalAberta = false;
  }

  onArquivoNovaDemandaSelecionado(event: Event): void {
    const input = event.target as HTMLInputElement;
    this.anexoNovaDemanda = input.files?.[0] ?? null;
  }

  async salvarNovaDemanda(): Promise<void> {
    if (!this.novaDemanda.titulo || !this.novaDemanda.descricao) return;

    this.salvando = true;

    let demandaCriada: Demanda;
    try {
      demandaCriada = await firstValueFrom(this.demandaService.criarDemanda(this.novaDemanda));
    } catch {
      this.salvando = false;
      this.cdr.markForCheck();
      return;
    }

    const avisos: string[] = [];

    if (this.fiscalSelecionadoId) {
      try {
        await firstValueFrom(this.demandaService.delegarDemanda(demandaCriada.id!, this.fiscalSelecionadoId));
      } catch (err: any) {
        avisos.push('Não foi possível delegar ao fiscal escolhido: ' + (err?.error?.message || 'verifique se ele está ativo.'));
      }
    }

    if (this.anexoNovaDemanda) {
      try {
        const hash = await this.anexoService.calcularHashSha256(this.anexoNovaDemanda);
        await firstValueFrom(this.anexoService.fazerUpload(demandaCriada.id!, this.anexoNovaDemanda, hash));
      } catch (err: any) {
        avisos.push('Não foi possível anexar o arquivo: ' + (err?.error?.message || 'tente novamente pela fila de demandas.'));
      }
    }

    this.salvando = false;
    this.fecharModalNovaDemanda();
    this.carregarDados();

    if (avisos.length > 0) {
      alert('A demanda foi criada, mas:\n' + avisos.join('\n'));
    }
  }

  abrirModalEdicao(demanda: Demanda): void {
    this.demandaEmEdicao = { ...demanda };
    this.modalEdicaoAberta = true;
  }

  fecharModalEdicao(): void {
    this.modalEdicaoAberta = false;
    this.demandaEmEdicao = null;
  }

  async salvarEdicaoDemanda(): Promise<void> {
    if (!this.demandaEmEdicao || !this.demandaEmEdicao.titulo || !this.demandaEmEdicao.descricao) return;

    this.editando = true;
    try {
      await firstValueFrom(this.demandaService.editarDemanda(this.demandaEmEdicao.id!, {
        titulo: this.demandaEmEdicao.titulo,
        descricao: this.demandaEmEdicao.descricao,
        localizacao: this.demandaEmEdicao.localizacao,
        urgencia: this.demandaEmEdicao.urgencia
      }));
      this.fecharModalEdicao();
      this.carregarDados();
    } catch (err: any) {
      alert(err?.error || 'Não foi possível editar a demanda.');
    } finally {
      this.editando = false;
      this.cdr.markForCheck();
    }
  }

  delegar(demandaId: string, fiscalId: string): void {
    if (!fiscalId) return;

    this.demandaService.delegarDemanda(demandaId, fiscalId).subscribe({
      next: () => this.carregarDados()
    });
  }

  excluirDemanda(demanda: Demanda): void {
    if (!confirm(`Excluir a demanda "${demanda.titulo}"? Esta ação não pode ser desfeita.`)) return;

    this.demandaService.excluirDemanda(demanda.id!).subscribe({
      next: () => this.carregarDados(),
      error: (err) => alert(err?.error || 'Não foi possível excluir a demanda.')
    });
  }

  logout(): void {
    this.authService.logout();
  }

  abrirDocumentos(): void {
    window.open('/documentos', '_blank');
  }

  toggleFiscalExpandido(fiscalId: string): void {
    this.fiscalExpandidoId = this.fiscalExpandidoId === fiscalId ? null : fiscalId;
  }

  async abrirDocumento(doc: DocumentoPontuacao): Promise<void> {
    this.tipoDocumentoSelecionado = doc.tipo;
    this.documentoSelecionado = null;
    this.modalDocumentoAberto = true;
    this.carregandoDocumento = true;
    try {
      this.documentoSelecionado = doc.tipo === 'AUTO'
        ? await firstValueFrom(this.autoService.buscarPorId(doc.id))
        : await firstValueFrom(this.relatorioService.buscarPorId(doc.id));
    } catch {
      alert('Não foi possível carregar o documento.');
      this.modalDocumentoAberto = false;
    } finally {
      this.carregandoDocumento = false;
      this.cdr.markForCheck();
    }
  }

  fecharModalDocumento(): void {
    this.modalDocumentoAberto = false;
    this.documentoSelecionado = null;
    this.tipoDocumentoSelecionado = null;
  }

  asAuto(doc: AutoFiscalizacao | Relatorio | null): AutoFiscalizacao {
    return doc as AutoFiscalizacao;
  }

  asRelatorio(doc: AutoFiscalizacao | Relatorio | null): Relatorio {
    return doc as Relatorio;
  }

  async abrirAnexo(anexo: Anexo): Promise<void> {
    const aba = window.open('', '_blank');
    try {
      const blob = await firstValueFrom(this.anexoService.baixarArquivo(anexo.id!));
      const url = URL.createObjectURL(blob);
      if (aba) {
        aba.location.href = url;
      }
      setTimeout(() => URL.revokeObjectURL(url), 60000);
    } catch {
      aba?.close();
      alert('Não foi possível abrir o anexo.');
    }
  }
}
