import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../services/auth.service';
import { DemandaService } from '../../services/demanda.service';
import { AutoService } from '../../services/auto.service';
import { RelatorioService } from '../../services/relatorio.service';
import { UsuarioService } from '../../services/usuario.service';
import { ContribuinteService } from '../../services/contribuinte.service';
import { ImovelService } from '../../services/imovel.service';
import { AutoPreviewComponent } from '../auto-preview/auto-preview.component';
import { RelatorioPreviewComponent } from '../relatorio-preview/relatorio-preview.component';
import { DocumentoAssinadoUploadComponent } from '../documento-assinado-upload/documento-assinado-upload.component';
import { Demanda, AutoFiscalizacao, Relatorio, Usuario, Contribuinte, Imovel } from '../../models/models';

interface ImovelFormState {
  id?: string;
  inscricao: string;
  rua: string;
  numero: string;
  bairro: string;
  cep: string;
}

interface ContribuinteFormState {
  id?: string;
  nome: string;
  cpfCnpj: string;
  rua: string;
  numero: string;
  bairro: string;
  municipio: string;
  cep: string;
}

@Component({
  selector: 'app-fiscal-dashboard',
  standalone: true,
  imports: [CommonModule, FormsModule, AutoPreviewComponent, RelatorioPreviewComponent, DocumentoAssinadoUploadComponent],
  templateUrl: './fiscal-dashboard.component.html',
  styleUrls: ['./fiscal-dashboard.component.css']
})
export class FiscalDashboardComponent implements OnInit {
  currentUser: any = null;
  minhasDemandas: Demanda[] = [];
  demandaSelecionada: Demanda | null = null;
  autosDemanda: AutoFiscalizacao[] = [];
  relatoriosDemanda: Relatorio[] = [];
  loading: boolean = true;

  modalAutoAberta: boolean = false;
  salvandoAuto: boolean = false;
  novoAuto: Partial<AutoFiscalizacao> = {};
  editandoAutoId: string | null = null;
  contribuinteForm: ContribuinteFormState = { nome: '', cpfCnpj: '', rua: '', numero: '', bairro: '', municipio: '', cep: '' };
  imovelForm: ImovelFormState = { inscricao: '', rua: '', numero: '', bairro: '', cep: '' };

  resultadosBuscaContribuinte: Contribuinte[] = [];
  resultadosBuscaImovel: Imovel[] = [];
  imoveisDoContribuinte: Imovel[] = [];
  resultadosBuscaImovelRelatorio: Imovel[] = [];
  private buscaTimer: any;

  fiscaisDisponiveis: Usuario[] = [];

  modalDocumentoGeradoAberto: boolean = false;
  tipoDocumentoGerado: 'AUTO' | 'RELATORIO' | null = null;
  autoGerado: AutoFiscalizacao | null = null;
  relatorioGerado: Relatorio | null = null;
  modoVisualizacaoDocumento: boolean = false;

  modalRelatorioAberta: boolean = false;
  salvandoRelatorio: boolean = false;
  novoRelatorio: Partial<Relatorio> = {};
  editandoRelatorioId: string | null = null;
  relatorioImovelForm: ImovelFormState = { inscricao: '', rua: '', numero: '', bairro: '', cep: '' };
  imagensRelatorio: { file: File | null; legenda: string; previewUrl?: string }[] = [];

  termoBusca: string = '';
  abaAtiva: 'ATIVAS' | 'CONCLUIDAS' = 'ATIVAS';

  constructor(
    private authService: AuthService,
    private demandaService: DemandaService,
    private autoService: AutoService,
    private relatorioService: RelatorioService,
    private usuarioService: UsuarioService,
    private contribuinteService: ContribuinteService,
    private imovelService: ImovelService,
    private cdr: ChangeDetectorRef
  ) {}

  get demandasAtivas(): Demanda[] {
    return this.minhasDemandas.filter(d => d.status !== 'CONCLUIDO');
  }

  get demandasConcluidas(): Demanda[] {
    return this.minhasDemandas.filter(d => d.status === 'CONCLUIDO');
  }

  get demandasDaAbaAtiva(): Demanda[] {
    return this.abaAtiva === 'ATIVAS' ? this.demandasAtivas : this.demandasConcluidas;
  }

  get demandasFiltradas(): Demanda[] {
    const termo = this.termoBusca.trim().toLowerCase();
    if (!termo) return this.demandasDaAbaAtiva;
    return this.demandasDaAbaAtiva.filter(d =>
      d.titulo.toLowerCase().includes(termo) ||
      d.descricao.toLowerCase().includes(termo)
    );
  }

  selecionarAba(aba: 'ATIVAS' | 'CONCLUIDAS'): void {
    this.abaAtiva = aba;
    this.demandaSelecionada = null;
  }

  get autoJaAssinado(): boolean {
    return !!this.autosDemanda[0]?.documentoAssinadoNome;
  }

  get relatorioJaAssinado(): boolean {
    return !!this.relatoriosDemanda[0]?.documentoAssinadoNome;
  }

  get demandaConcluida(): boolean {
    return this.demandaSelecionada?.status === 'CONCLUIDO';
  }

  get podeFinalizarDemanda(): boolean {
    return !this.demandaConcluida && this.autoJaAssinado && this.relatorioJaAssinado;
  }

  get imovelBloqueadoAuto(): boolean {
    return this.relatoriosDemanda.length > 0;
  }

  get imovelBloqueadoRelatorio(): boolean {
    return this.autosDemanda.length > 0;
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

  abrirMinhaPontuacao(): void {
    window.open('/minha-pontuacao', '_blank');
  }

  ngOnInit(): void {
    this.currentUser = this.authService.currentUser();
    this.carregarDemandas();

    this.usuarioService.listarTodos().subscribe({
      next: (res) => {
        this.fiscaisDisponiveis = res.filter(u => u.cargo === 'FISCAL' && u.nome !== this.currentUser?.nome);
        this.cdr.markForCheck();
      }
    });
  }

  carregarDemandas(): void {
    this.loading = true;
    this.demandaService.listarMinhasDemandas().subscribe({
      next: (res) => {
        this.minhasDemandas = res;
        this.loading = false;
        this.cdr.markForCheck();
      },
      error: () => {
        this.loading = false;
        this.cdr.markForCheck();
      }
    });
  }

  selecionarDemanda(demanda: Demanda): void {
    this.demandaSelecionada = demanda;
    this.carregarAutosERelatorios(demanda.id!);
  }

  carregarAutosERelatorios(demandaId: string): void {
    this.autoService.listarPorDemanda(demandaId).subscribe({
      next: (res) => {
        this.autosDemanda = res;
        this.cdr.markForCheck();
      }
    });
    this.relatorioService.listarPorDemanda(demandaId).subscribe({
      next: (res) => {
        this.relatoriosDemanda = res;
        this.cdr.markForCheck();
      }
    });
  }

  finalizarDemanda(): void {
    if (!this.demandaSelecionada?.id || !this.podeFinalizarDemanda) return;
    if (!confirm('Finalizar esta demanda? Depois de finalizada, o Auto e o Relatório não poderão mais ser alterados.')) return;

    this.demandaService.finalizarDemanda(this.demandaSelecionada.id).subscribe({
      next: (demandaAtualizada) => {
        this.demandaSelecionada = demandaAtualizada;
        this.minhasDemandas = this.minhasDemandas.map(d => d.id === demandaAtualizada.id ? demandaAtualizada : d);
        this.cdr.markForCheck();
      },
      error: (err) => alert('Erro ao finalizar demanda: ' + (err.error?.message || 'Tente novamente.'))
    });
  }

  onAutoAtualizado(doc: AutoFiscalizacao): void {
    if (this.autoGerado?.id === doc.id) {
      this.autoGerado = doc;
    }
    this.autosDemanda = this.autosDemanda.map(a => a.id === doc.id ? doc : a);
    this.cdr.markForCheck();
  }

  onRelatorioAtualizado(doc: Relatorio): void {
    if (this.relatorioGerado?.id === doc.id) {
      this.relatorioGerado = doc;
    }
    this.relatoriosDemanda = this.relatoriosDemanda.map(r => r.id === doc.id ? doc : r);
    this.cdr.markForCheck();
  }

  private preencherContribuinteForm(c?: Contribuinte): void {
    this.contribuinteForm = {
      id: c?.id,
      nome: c?.nome || '',
      cpfCnpj: c?.cpfCnpj || '',
      rua: c?.rua || '',
      numero: c?.numero || '',
      bairro: c?.bairro || '',
      municipio: c?.municipio || '',
      cep: c?.cep || ''
    };
  }

  limparSelecaoContribuinte(): void {
    this.preencherContribuinteForm();
  }

  private preencherImovelForm(i?: Imovel): void {
    this.imovelForm = {
      id: i?.id,
      inscricao: i?.inscricao || '',
      rua: i?.rua || '',
      numero: i?.numero || '',
      bairro: i?.bairro || '',
      cep: i?.cep || ''
    };
  }

  private preencherImovelFormRelatorio(i?: Imovel): void {
    this.relatorioImovelForm = {
      id: i?.id,
      inscricao: i?.inscricao || '',
      rua: i?.rua || '',
      numero: i?.numero || '',
      bairro: i?.bairro || '',
      cep: i?.cep || ''
    };
  }

  limparSelecaoImovel(): void {
    this.preencherImovelForm();
  }

  limparSelecaoImovelRelatorio(): void {
    this.preencherImovelFormRelatorio();
  }

  abrirModalAuto(): void {
    this.editandoAutoId = null;
    this.novoAuto = {
      processoAdministrativo: '',
      irregularidadesConstatadas: '',
      dispositivosLegais: '',
      penalidades: '',
      prazoDefesa: 15,
      temTestemunhas: false
    };
    this.preencherContribuinteForm();
    this.preencherImovelForm();
    this.resultadosBuscaContribuinte = [];
    this.resultadosBuscaImovel = [];
    this.imoveisDoContribuinte = [];

    const relatorioExistente = this.relatoriosDemanda[0];
    if (relatorioExistente?.imovel) {
      this.preencherImovelForm(relatorioExistente.imovel);
      if (relatorioExistente.imovel.contribuinte) {
        this.preencherContribuinteForm(relatorioExistente.imovel.contribuinte);
      }
    }

    this.modalAutoAberta = true;
  }

  abrirModalEditarAuto(auto: AutoFiscalizacao): void {
    if (auto.documentoAssinadoNome) {
      alert('Este Auto já foi assinado e não pode mais ser editado.');
      return;
    }
    this.editandoAutoId = auto.id || null;
    this.novoAuto = {
      processoAdministrativo: auto.processoAdministrativo,
      irregularidadesConstatadas: auto.irregularidadesConstatadas,
      providencias: auto.providencias,
      dispositivosLegais: auto.dispositivosLegais,
      penalidades: auto.penalidades,
      prazoDefesa: auto.prazoDefesa,
      temTestemunhas: auto.temTestemunhas,
      nomeTestemunha1: auto.nomeTestemunha1,
      cpfTestemunha1: auto.cpfTestemunha1,
      nomeTestemunha2: auto.nomeTestemunha2,
      cpfTestemunha2: auto.cpfTestemunha2
    };
    this.preencherContribuinteForm(auto.contribuinte);
    this.preencherImovelForm(auto.imovel);
    this.resultadosBuscaContribuinte = [];
    this.resultadosBuscaImovel = [];
    this.imoveisDoContribuinte = [];
    this.modalAutoAberta = true;
  }

  fecharModalAuto(): void {
    this.modalAutoAberta = false;
    this.editandoAutoId = null;
  }

  private buscarComDebounce(termo: string, buscar: (t: string) => void): void {
    clearTimeout(this.buscaTimer);
    if (!termo || termo.trim().length < 2) {
      buscar('');
      return;
    }
    this.buscaTimer = setTimeout(() => buscar(termo.trim()), 300);
  }

  buscarContribuinte(termo: string): void {
    this.buscarComDebounce(termo, (t) => {
      if (!t) {
        this.resultadosBuscaContribuinte = [];
        this.cdr.markForCheck();
        return;
      }
      this.contribuinteService.buscar(t).subscribe({
        next: (res) => {
          this.resultadosBuscaContribuinte = res;
          this.cdr.markForCheck();
        }
      });
    });
  }

  selecionarContribuinteExistente(c: Contribuinte): void {
    this.preencherContribuinteForm(c);
    this.resultadosBuscaContribuinte = [];

    this.imoveisDoContribuinte = [];
    if (c.id) {
      this.imovelService.buscarPorContribuinte(c.id).subscribe({
        next: (res) => {
          this.imoveisDoContribuinte = res;
          this.cdr.markForCheck();
        }
      });
    }
  }

  buscarImovel(termo: string): void {
    this.buscarComDebounce(termo, (t) => {
      if (!t) {
        this.resultadosBuscaImovel = [];
        this.cdr.markForCheck();
        return;
      }
      this.imovelService.buscar(t).subscribe({
        next: (res) => {
          this.resultadosBuscaImovel = res;
          this.cdr.markForCheck();
        }
      });
    });
  }

  selecionarImovelExistente(i: Imovel): void {
    this.preencherImovelForm(i);
    if (i.contribuinte) {
      this.preencherContribuinteForm(i.contribuinte);
    }
    this.resultadosBuscaImovel = [];
  }

  buscarImovelRelatorio(termo: string): void {
    this.buscarComDebounce(termo, (t) => {
      if (!t) {
        this.resultadosBuscaImovelRelatorio = [];
        this.cdr.markForCheck();
        return;
      }
      this.imovelService.buscar(t).subscribe({
        next: (res) => {
          this.resultadosBuscaImovelRelatorio = res;
          this.cdr.markForCheck();
        }
      });
    });
  }

  selecionarImovelExistenteRelatorio(i: Imovel): void {
    this.preencherImovelFormRelatorio(i);
    this.resultadosBuscaImovelRelatorio = [];
  }

  selecionarTestemunha1(fiscalId: string): void {
    const fiscal = this.fiscaisDisponiveis.find(f => f.id === fiscalId);
    if (!fiscal) return;
    this.novoAuto.nomeTestemunha1 = fiscal.nome;
    this.novoAuto.cpfTestemunha1 = fiscal.cpf;
  }

  selecionarTestemunha2(fiscalId: string): void {
    const fiscal = this.fiscaisDisponiveis.find(f => f.id === fiscalId);
    if (!fiscal) return;
    this.novoAuto.nomeTestemunha2 = fiscal.nome;
    this.novoAuto.cpfTestemunha2 = fiscal.cpf;
  }

  emitirAutoSubmit(): void {
    if (!this.demandaSelecionada) return;

    this.salvandoAuto = true;
    const payload: Partial<AutoFiscalizacao> = {
      ...this.novoAuto,
      demanda: { id: this.demandaSelecionada.id } as any,
      contribuinte: this.contribuinteForm as any,
      imovel: this.imovelForm as any
    };

    const editando = !!this.editandoAutoId;
    const request$ = editando
      ? this.autoService.atualizarAuto(this.editandoAutoId!, payload)
      : this.autoService.emitirAuto(payload);

    request$.subscribe({
      next: (autoSalvo) => {
        this.salvandoAuto = false;
        this.fecharModalAuto();
        this.tipoDocumentoGerado = 'AUTO';
        this.autoGerado = autoSalvo;
        this.modoVisualizacaoDocumento = editando;
        this.modalDocumentoGeradoAberto = true;
        this.carregarDemandas();
        this.carregarAutosERelatorios(this.demandaSelecionada!.id!);
        this.cdr.markForCheck();
      },
      error: (err) => {
        this.salvandoAuto = false;
        this.cdr.markForCheck();
        alert(`Erro ao ${editando ? 'editar' : 'emitir'} auto: ` + (err.error?.message || 'Verifique os campos.'));
      }
    });
  }

  fecharModalDocumentoGerado(): void {
    this.modalDocumentoGeradoAberto = false;
    this.tipoDocumentoGerado = null;
    this.autoGerado = null;
    this.relatorioGerado = null;
    this.modoVisualizacaoDocumento = false;
  }

  verDocumentoAuto(auto: AutoFiscalizacao): void {
    this.tipoDocumentoGerado = 'AUTO';
    this.autoGerado = auto;
    this.modoVisualizacaoDocumento = true;
    this.modalDocumentoGeradoAberto = true;
  }

  verDocumentoRelatorio(relatorio: Relatorio): void {
    this.tipoDocumentoGerado = 'RELATORIO';
    this.relatorioGerado = relatorio;
    this.modoVisualizacaoDocumento = true;
    this.modalDocumentoGeradoAberto = true;
  }

  excluirAuto(auto: AutoFiscalizacao): void {
    if (!auto.id) return;
    if (!confirm(`Excluir o Auto Nº ${auto.numeroSequencial}/${auto.ano}? O número será liberado para reuso.`)) return;

    this.autoService.excluirAuto(auto.id).subscribe({
      next: () => {
        this.autosDemanda = this.autosDemanda.filter(a => a.id !== auto.id);
        this.cdr.markForCheck();
      },
      error: (err) => alert('Erro ao excluir auto: ' + (err.error?.message || 'Tente novamente.'))
    });
  }

  excluirRelatorio(relatorio: Relatorio): void {
    if (!relatorio.id) return;
    if (!confirm(`Excluir o Relatório Nº ${relatorio.numeroSequencial}/${relatorio.ano}? O número será liberado para reuso.`)) return;

    this.relatorioService.excluirRelatorio(relatorio.id).subscribe({
      next: () => {
        this.relatoriosDemanda = this.relatoriosDemanda.filter(r => r.id !== relatorio.id);
        this.cdr.markForCheck();
      },
      error: (err) => alert('Erro ao excluir relatório: ' + (err.error?.message || 'Tente novamente.'))
    });
  }

  abrirModalRelatorio(): void {
    this.editandoRelatorioId = null;
    this.novoRelatorio = {
      dataHoraVistoria: new Date().toISOString().substring(0, 16),
      assunto: '',
      atendimento: '',
      processoAdministrativo: '',
      textoVistoria: ''
    };
    this.preencherImovelFormRelatorio();
    this.imagensRelatorio = [];
    this.resultadosBuscaImovelRelatorio = [];

    const autoExistente = this.autosDemanda[0];
    if (autoExistente?.imovel) {
      this.preencherImovelFormRelatorio(autoExistente.imovel);
    }

    this.modalRelatorioAberta = true;
  }

  abrirModalEditarRelatorio(relatorio: Relatorio): void {
    if (relatorio.documentoAssinadoNome) {
      alert('Este Relatório já foi assinado e não pode mais ser editado.');
      return;
    }
    this.editandoRelatorioId = relatorio.id || null;
    this.novoRelatorio = {
      dataHoraVistoria: (relatorio.dataHoraVistoria || new Date().toISOString()).substring(0, 16),
      assunto: relatorio.assunto || '',
      atendimento: relatorio.atendimento || '',
      processoAdministrativo: relatorio.processoAdministrativo || '',
      textoVistoria: relatorio.textoVistoria || ''
    };
    this.preencherImovelFormRelatorio(relatorio.imovel);
    this.imagensRelatorio = (relatorio.imagens || []).map(img => ({
      file: null,
      legenda: img.legenda || '',
      previewUrl: img.imagemBase64
    }));
    this.resultadosBuscaImovelRelatorio = [];
    this.modalRelatorioAberta = true;
  }

  fecharModalRelatorio(): void {
    this.modalRelatorioAberta = false;
    this.editandoRelatorioId = null;
  }

  adicionarLinhaImagemRelatorio(): void {
    this.imagensRelatorio.push({ file: null, legenda: '' });
  }

  removerLinhaImagemRelatorio(index: number): void {
    this.imagensRelatorio.splice(index, 1);
  }

  onImagemRelatorioSelecionada(index: number, event: Event): void {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0];
    if (!file) return;

    this.imagensRelatorio[index].file = file;
    const reader = new FileReader();
    reader.onload = () => {
      this.imagensRelatorio[index].previewUrl = reader.result as string;
      this.cdr.markForCheck();
    };
    reader.readAsDataURL(file);
  }

  emitirRelatorioSubmit(): void {
    if (!this.demandaSelecionada) return;

    this.salvandoRelatorio = true;
    const payload: Partial<Relatorio> = {
      ...this.novoRelatorio,
      demanda: { id: this.demandaSelecionada.id } as any,
      imovel: this.relatorioImovelForm as any,
      imagens: this.imagensRelatorio
        .filter(i => !!i.previewUrl)
        .map((i, ordem) => ({ imagemBase64: i.previewUrl!, legenda: i.legenda, ordem })) as any
    };

    const editando = !!this.editandoRelatorioId;
    const request$ = editando
      ? this.relatorioService.atualizarRelatorio(this.editandoRelatorioId!, payload)
      : this.relatorioService.emitirRelatorio(payload);

    request$.subscribe({
      next: (relatorioSalvo) => {
        this.salvandoRelatorio = false;
        this.fecharModalRelatorio();
        this.tipoDocumentoGerado = 'RELATORIO';
        this.relatorioGerado = relatorioSalvo;
        this.modoVisualizacaoDocumento = editando;
        this.modalDocumentoGeradoAberto = true;
        this.carregarDemandas();
        this.carregarAutosERelatorios(this.demandaSelecionada!.id!);
        this.cdr.markForCheck();
      },
      error: (err) => {
        this.salvandoRelatorio = false;
        this.cdr.markForCheck();
        alert(`Erro ao ${editando ? 'editar' : 'emitir'} relatório: ` + (err.error?.message || 'Verifique os campos.'));
      }
    });
  }

  logout(): void {
    this.authService.logout();
  }
}
