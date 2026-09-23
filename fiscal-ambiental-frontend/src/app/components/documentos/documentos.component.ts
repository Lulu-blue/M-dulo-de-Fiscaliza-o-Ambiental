import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { firstValueFrom } from 'rxjs';
import { AuthService } from '../../services/auth.service';
import { AutoService } from '../../services/auto.service';
import { RelatorioService } from '../../services/relatorio.service';
import { AutoPreviewComponent } from '../auto-preview/auto-preview.component';
import { RelatorioPreviewComponent } from '../relatorio-preview/relatorio-preview.component';
import { AutoFiscalizacao, Relatorio } from '../../models/models';

interface DocumentoUnificado {
  tipo: 'AUTO' | 'RELATORIO';
  id: string;
  numeroSequencial?: number;
  ano?: number;
  demandaTitulo?: string;
  fiscalNomes: string;
  pontos: number;
  dataGeracao?: string;
  original: AutoFiscalizacao | Relatorio;
}

@Component({
  selector: 'app-documentos',
  standalone: true,
  imports: [CommonModule, FormsModule, AutoPreviewComponent, RelatorioPreviewComponent],
  templateUrl: './documentos.component.html',
  styleUrls: ['./documentos.component.css']
})
export class DocumentosComponent implements OnInit {
  currentUser: any = null;
  loading: boolean = true;
  documentos: DocumentoUnificado[] = [];

  termoBusca: string = '';
  filtroTipo: 'TODOS' | 'AUTO' | 'RELATORIO' = 'TODOS';

  modalAberto: boolean = false;
  documentoSelecionado: DocumentoUnificado | null = null;

  constructor(
    private authService: AuthService,
    private autoService: AutoService,
    private relatorioService: RelatorioService,
    private cdr: ChangeDetectorRef
  ) {}

  get documentosFiltrados(): DocumentoUnificado[] {
    const termo = this.termoBusca.trim().toLowerCase();
    return this.documentos.filter(d => {
      if (this.filtroTipo !== 'TODOS' && d.tipo !== this.filtroTipo) return false;
      if (!termo) return true;
      return (d.demandaTitulo || '').toLowerCase().includes(termo) ||
        d.fiscalNomes.toLowerCase().includes(termo) ||
        String(d.numeroSequencial ?? '').includes(termo);
    });
  }

  ngOnInit(): void {
    this.currentUser = this.authService.currentUser();
    this.carregar();
  }

  async carregar(): Promise<void> {
    this.loading = true;
    this.cdr.markForCheck();
    try {
      const [autos, relatorios] = await Promise.all([
        firstValueFrom(this.autoService.listarTodos()),
        firstValueFrom(this.relatorioService.listarTodos())
      ]);

      const docsAuto: DocumentoUnificado[] = autos.map(a => ({
        tipo: 'AUTO' as const,
        id: a.id!,
        numeroSequencial: a.numeroSequencial,
        ano: a.ano,
        demandaTitulo: a.demanda?.titulo,
        fiscalNomes: a.criador?.nome || '',
        pontos: a.pontosProdutividade || 0,
        dataGeracao: a.dataGeracao,
        original: a
      }));

      const docsRelatorio: DocumentoUnificado[] = relatorios.map(r => ({
        tipo: 'RELATORIO' as const,
        id: r.id!,
        numeroSequencial: r.numeroSequencial,
        ano: r.ano,
        demandaTitulo: r.demanda?.titulo,
        fiscalNomes: r.criador?.nome || '',
        pontos: r.pontosProdutividadeBase || 0,
        dataGeracao: r.dataGeracao,
        original: r
      }));

      this.documentos = [...docsAuto, ...docsRelatorio].sort((a, b) =>
        new Date(b.dataGeracao || 0).getTime() - new Date(a.dataGeracao || 0).getTime()
      );
    } finally {
      this.loading = false;
      this.cdr.markForCheck();
    }
  }

  abrirDetalhe(doc: DocumentoUnificado): void {
    this.documentoSelecionado = doc;
    this.modalAberto = true;
  }

  fecharDetalhe(): void {
    this.modalAberto = false;
    this.documentoSelecionado = null;
  }

  asAuto(doc: DocumentoUnificado | null): AutoFiscalizacao {
    return doc!.original as AutoFiscalizacao;
  }

  asRelatorio(doc: DocumentoUnificado | null): Relatorio {
    return doc!.original as Relatorio;
  }

  logout(): void {
    this.authService.logout();
  }
}
