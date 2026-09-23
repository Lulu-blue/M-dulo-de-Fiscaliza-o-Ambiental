import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, from } from 'rxjs';
import { switchMap } from 'rxjs/operators';
import { Relatorio } from '../models/models';

@Injectable({
  providedIn: 'root'
})
export class RelatorioService {
  private apiUrl = 'http://localhost:8080/api';

  constructor(private http: HttpClient) {}

  emitirRelatorio(relatorio: Partial<Relatorio>): Observable<Relatorio> {
    return this.http.post<Relatorio>(`${this.apiUrl}/fiscal/relatorios`, relatorio);
  }

  buscarPorId(id: string): Observable<Relatorio> {
    return this.http.get<Relatorio>(`${this.apiUrl}/relatorios/${id}`);
  }

  listarTodos(): Observable<Relatorio[]> {
    return this.http.get<Relatorio[]>(`${this.apiUrl}/relatorios`);
  }

  listarPorDemanda(demandaId: string): Observable<Relatorio[]> {
    return this.http.get<Relatorio[]>(`${this.apiUrl}/fiscal/relatorios/por-demanda/${demandaId}`);
  }

  atualizarRelatorio(relatorioId: string, relatorio: Partial<Relatorio>): Observable<Relatorio> {
    return this.http.put<Relatorio>(`${this.apiUrl}/fiscal/relatorios/${relatorioId}`, relatorio);
  }

  private arquivoParaBase64(file: File): Promise<string> {
    return new Promise((resolve, reject) => {
      const reader = new FileReader();
      reader.onload = () => {
        const resultado = reader.result as string;
        resolve(resultado.split(',')[1] ?? '');
      };
      reader.onerror = reject;
      reader.readAsDataURL(file);
    });
  }

  anexarDocumentoAssinado(relatorioId: string, file: File): Observable<Relatorio> {
    return from(this.arquivoParaBase64(file)).pipe(
      switchMap(conteudoBase64 => this.http.post<Relatorio>(`${this.apiUrl}/fiscal/relatorios/${relatorioId}/documento-assinado`, {
        nomeArquivo: file.name,
        contentType: file.type || 'application/octet-stream',
        conteudoBase64
      }))
    );
  }

  removerDocumentoAssinado(relatorioId: string): Observable<Relatorio> {
    return this.http.delete<Relatorio>(`${this.apiUrl}/fiscal/relatorios/${relatorioId}/documento-assinado`);
  }

  urlDocumentoAssinado(relatorioId: string): string {
    return `${this.apiUrl}/relatorios/${relatorioId}/documento-assinado`;
  }

  baixarDocumentoAssinado(relatorioId: string): Observable<Blob> {
    return this.http.get(this.urlDocumentoAssinado(relatorioId), { responseType: 'blob' });
  }

  excluirRelatorio(relatorioId: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/fiscal/relatorios/${relatorioId}`);
  }
}
