import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, from } from 'rxjs';
import { switchMap } from 'rxjs/operators';
import { Anexo } from '../models/models';

@Injectable({
  providedIn: 'root'
})
export class AnexoService {
  private apiUrl = 'http://localhost:8080/api/anexos';

  constructor(private http: HttpClient) {}

  async calcularHashSha256(file: File): Promise<string> {
    const arrayBuffer = await file.arrayBuffer();
    const hashBuffer = await crypto.subtle.digest('SHA-256', arrayBuffer);
    const hashArray = Array.from(new Uint8Array(hashBuffer));
    return hashArray.map(b => b.toString(16).padStart(2, '0')).join('');
  }

  verificarDuplicidade(hashSha256: string): Observable<{ existe: boolean }> {
    return this.http.get<{ existe: boolean }>(`${this.apiUrl}/verificar-hash/${hashSha256}`);
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

  fazerUpload(demandaId: string, file: File, hashSha256: string): Observable<Anexo> {
    return from(this.arquivoParaBase64(file)).pipe(
      switchMap(conteudoBase64 => {
        const payload = {
          demandaId,
          nomeArquivo: file.name,
          hashSha256,
          tamanhoBytes: file.size,
          contentType: file.type || 'application/octet-stream',
          conteudoBase64
        };
        return this.http.post<Anexo>(this.apiUrl, payload);
      })
    );
  }

  listarPorDemanda(demandaId: string): Observable<Anexo[]> {
    return this.http.get<Anexo[]>(`${this.apiUrl}/demanda/${demandaId}`);
  }

  urlArquivo(anexoId: string): string {
    return `${this.apiUrl}/${anexoId}/arquivo`;
  }

  // exige token: navegação direta do navegador não envia Authorization
  baixarArquivo(anexoId: string): Observable<Blob> {
    return this.http.get(this.urlArquivo(anexoId), { responseType: 'blob' });
  }
}
