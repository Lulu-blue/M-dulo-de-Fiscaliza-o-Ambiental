import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, from } from 'rxjs';
import { switchMap } from 'rxjs/operators';
import { AutoFiscalizacao } from '../models/models';

@Injectable({
  providedIn: 'root'
})
export class AutoService {
  private apiUrl = 'http://localhost:8080/api';

  constructor(private http: HttpClient) {}

  emitirAuto(auto: Partial<AutoFiscalizacao>): Observable<AutoFiscalizacao> {
    return this.http.post<AutoFiscalizacao>(`${this.apiUrl}/fiscal/autos`, auto);
  }

  buscarPorId(id: string): Observable<AutoFiscalizacao> {
    return this.http.get<AutoFiscalizacao>(`${this.apiUrl}/autos/${id}`);
  }

  listarTodos(): Observable<AutoFiscalizacao[]> {
    return this.http.get<AutoFiscalizacao[]>(`${this.apiUrl}/autos`);
  }

  listarPorDemanda(demandaId: string): Observable<AutoFiscalizacao[]> {
    return this.http.get<AutoFiscalizacao[]>(`${this.apiUrl}/fiscal/autos/por-demanda/${demandaId}`);
  }

  atualizarAuto(autoId: string, auto: Partial<AutoFiscalizacao>): Observable<AutoFiscalizacao> {
    return this.http.put<AutoFiscalizacao>(`${this.apiUrl}/fiscal/autos/${autoId}`, auto);
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

  anexarDocumentoAssinado(autoId: string, file: File): Observable<AutoFiscalizacao> {
    return from(this.arquivoParaBase64(file)).pipe(
      switchMap(conteudoBase64 => this.http.post<AutoFiscalizacao>(`${this.apiUrl}/fiscal/autos/${autoId}/documento-assinado`, {
        nomeArquivo: file.name,
        contentType: file.type || 'application/octet-stream',
        conteudoBase64
      }))
    );
  }

  removerDocumentoAssinado(autoId: string): Observable<AutoFiscalizacao> {
    return this.http.delete<AutoFiscalizacao>(`${this.apiUrl}/fiscal/autos/${autoId}/documento-assinado`);
  }

  urlDocumentoAssinado(autoId: string): string {
    return `${this.apiUrl}/autos/${autoId}/documento-assinado`;
  }

  baixarDocumentoAssinado(autoId: string): Observable<Blob> {
    return this.http.get(this.urlDocumentoAssinado(autoId), { responseType: 'blob' });
  }

  excluirAuto(autoId: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/fiscal/autos/${autoId}`);
  }
}
