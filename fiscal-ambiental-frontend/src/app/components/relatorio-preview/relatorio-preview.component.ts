import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { DocumentoHeaderComponent } from '../documento-header/documento-header.component';
import { Relatorio } from '../../models/models';

@Component({
  selector: 'app-relatorio-preview',
  standalone: true,
  imports: [CommonModule, DocumentoHeaderComponent],
  templateUrl: './relatorio-preview.component.html',
  styleUrls: ['./relatorio-preview.component.css']
})
export class RelatorioPreviewComponent {
  @Input({ required: true }) relatorio!: Relatorio;

  get enderecoImovel(): string {
    const i = this.relatorio.imovel;
    if (!i) return '';
    return [i.rua, i.numero].filter(Boolean).join(', ');
  }

  get inscricaoLabel(): string {
    const inscricao = this.relatorio.imovel?.inscricao || '';
    return inscricao.replace(/\D/g, '').length === 14 ? 'CNPJ' : 'Inscrição Imobiliária';
  }

  get textoDataHoraVistoria(): string {
    if (!this.relatorio.dataHoraVistoria) return '';
    const data = new Date(this.relatorio.dataHoraVistoria);
    if (isNaN(data.getTime())) return '';
    const dataFmt = data.toLocaleDateString('pt-BR');
    const horaFmt = data.toLocaleTimeString('pt-BR', { hour: '2-digit', minute: '2-digit' });
    return ` no dia ${dataFmt} às ${horaFmt}`;
  }

  imprimir(): void {
    window.print();
  }
}
