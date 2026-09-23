import { Component, EventEmitter, Input, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { firstValueFrom } from 'rxjs';
import { AutoService } from '../../services/auto.service';
import { RelatorioService } from '../../services/relatorio.service';
import { AutoFiscalizacao, Relatorio } from '../../models/models';

const VINTE_QUATRO_HORAS_MS = 24 * 60 * 60 * 1000;

@Component({
  selector: 'app-documento-assinado-upload',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './documento-assinado-upload.component.html',
  styleUrls: ['./documento-assinado-upload.component.css']
})
export class DocumentoAssinadoUploadComponent {
  @Input({ required: true }) tipo!: 'AUTO' | 'RELATORIO';
  @Input({ required: true }) documentoId!: string;
  @Input() nomeArquivo?: string;
  @Input() enviadoEm?: string;
  @Input() pontos: number = 0;
  // true quando a demanda já foi finalizada — bloqueia a remoção mesmo dentro das 24h
  @Input() bloqueado: boolean = false;
  @Output() atualizado = new EventEmitter<AutoFiscalizacao | Relatorio>();

  enviando = false;
  removendo = false;

  constructor(
    private autoService: AutoService,
    private relatorioService: RelatorioService
  ) {}

  get temDocumento(): boolean {
    return !!this.nomeArquivo;
  }

  get prazoRemocao(): Date | null {
    if (!this.enviadoEm) return null;
    return new Date(new Date(this.enviadoEm).getTime() + VINTE_QUATRO_HORAS_MS);
  }

  get podeRemover(): boolean {
    if (this.bloqueado) return false;
    const prazo = this.prazoRemocao;
    return !!prazo && prazo.getTime() > Date.now();
  }

  async onArquivoSelecionado(event: Event): Promise<void> {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0];
    if (!file) return;

    this.enviando = true;
    try {
      const atualizado = this.tipo === 'AUTO'
        ? await firstValueFrom(this.autoService.anexarDocumentoAssinado(this.documentoId, file))
        : await firstValueFrom(this.relatorioService.anexarDocumentoAssinado(this.documentoId, file));
      this.atualizado.emit(atualizado);
    } catch (err: any) {
      alert(err?.error?.message || 'Não foi possível anexar o documento assinado.');
    } finally {
      this.enviando = false;
      input.value = '';
    }
  }

  async remover(): Promise<void> {
    if (!confirm('Remover o documento assinado? Você perderá a pontuação deste documento.')) return;

    this.removendo = true;
    try {
      const atualizado = this.tipo === 'AUTO'
        ? await firstValueFrom(this.autoService.removerDocumentoAssinado(this.documentoId))
        : await firstValueFrom(this.relatorioService.removerDocumentoAssinado(this.documentoId));
      this.atualizado.emit(atualizado);
    } catch (err: any) {
      alert(err?.error?.message || 'Não foi possível remover o documento assinado.');
    } finally {
      this.removendo = false;
    }
  }

  async abrirDocumento(): Promise<void> {
    const aba = window.open('', '_blank');
    try {
      const blob = this.tipo === 'AUTO'
        ? await firstValueFrom(this.autoService.baixarDocumentoAssinado(this.documentoId))
        : await firstValueFrom(this.relatorioService.baixarDocumentoAssinado(this.documentoId));
      const url = URL.createObjectURL(blob);
      if (aba) {
        aba.location.href = url;
      }
      setTimeout(() => URL.revokeObjectURL(url), 60000);
    } catch {
      aba?.close();
      alert('Não foi possível abrir o documento assinado.');
    }
  }
}
