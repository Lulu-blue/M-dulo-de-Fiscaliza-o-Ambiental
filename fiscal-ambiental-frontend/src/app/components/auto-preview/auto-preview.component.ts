import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { DocumentoHeaderComponent } from '../documento-header/documento-header.component';
import { AutoFiscalizacao } from '../../models/models';

@Component({
  selector: 'app-auto-preview',
  standalone: true,
  imports: [CommonModule, DocumentoHeaderComponent],
  templateUrl: './auto-preview.component.html',
  styleUrls: ['./auto-preview.component.css']
})
export class AutoPreviewComponent {
  @Input({ required: true }) auto!: AutoFiscalizacao;

  get enderecoAutuado(): string {
    const c = this.auto.contribuinte;
    if (!c) return '';
    return [c.rua, c.numero].filter(Boolean).join(', ');
  }

  get enderecoImovel(): string {
    const i = this.auto.imovel;
    if (!i) return '';
    return [i.rua, i.numero].filter(Boolean).join(', ');
  }

  imprimir(): void {
    window.print();
  }
}
