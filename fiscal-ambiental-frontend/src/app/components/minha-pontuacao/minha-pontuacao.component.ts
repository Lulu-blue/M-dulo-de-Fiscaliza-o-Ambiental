import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../services/auth.service';
import { DemandaService } from '../../services/demanda.service';
import { MinhaPontuacaoResposta, PontuacaoDocumento } from '../../models/models';

interface DiaGrafico {
  dia: string;
  pontos: number;
  label: string;
}

const DIAS_NO_GRAFICO = 14;

@Component({
  selector: 'app-minha-pontuacao',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './minha-pontuacao.component.html',
  styleUrls: ['./minha-pontuacao.component.css']
})
export class MinhaPontuacaoComponent implements OnInit {
  currentUser: any = null;
  loading = true;
  dados: MinhaPontuacaoResposta | null = null;
  grafico: DiaGrafico[] = [];

  constructor(
    private authService: AuthService,
    private demandaService: DemandaService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.currentUser = this.authService.currentUser();
    this.carregar();
  }

  get totalPontos(): number {
    return this.dados?.totalPontos || 0;
  }

  get documentos(): PontuacaoDocumento[] {
    return this.dados?.documentos || [];
  }

  get maxPontosGrafico(): number {
    return Math.max(1, ...this.grafico.map(d => d.pontos));
  }

  get mediaDiaria(): number {
    if (this.grafico.length === 0) return 0;
    const soma = this.grafico.reduce((acc, d) => acc + d.pontos, 0);
    return Math.round((soma / this.grafico.length) * 10) / 10;
  }

  carregar(): void {
    this.loading = true;
    this.demandaService.minhaPontuacao().subscribe({
      next: (res) => {
        this.dados = res;
        this.grafico = this.montarGrafico(res);
        this.loading = false;
        this.cdr.markForCheck();
      },
      error: () => {
        this.loading = false;
        this.cdr.markForCheck();
      }
    });
  }

  // nunca toISOString aqui: converte pra UTC e desalinha o dia à noite em fusos negativos
  private chaveDataLocal(d: Date): string {
    const ano = d.getFullYear();
    const mes = String(d.getMonth() + 1).padStart(2, '0');
    const dia = String(d.getDate()).padStart(2, '0');
    return `${ano}-${mes}-${dia}`;
  }

  private montarGrafico(res: MinhaPontuacaoResposta): DiaGrafico[] {
    const porDiaMap = new Map(res.porDia.map(d => [d.dia, d.pontos]));
    const dias: DiaGrafico[] = [];
    const hoje = new Date();

    for (let i = DIAS_NO_GRAFICO - 1; i >= 0; i--) {
      const data = new Date(hoje);
      data.setDate(data.getDate() - i);
      const chave = this.chaveDataLocal(data);
      dias.push({
        dia: chave,
        pontos: porDiaMap.get(chave) || 0,
        label: data.toLocaleDateString('pt-BR', { day: '2-digit', month: '2-digit' })
      });
    }

    return dias;
  }

  logout(): void {
    this.authService.logout();
  }
}
