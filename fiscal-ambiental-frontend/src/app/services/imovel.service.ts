import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Imovel } from '../models/models';

@Injectable({
  providedIn: 'root'
})
export class ImovelService {
  private apiUrl = 'http://localhost:8080/api/imoveis';

  constructor(private http: HttpClient) {}

  buscar(termo: string): Observable<Imovel[]> {
    return this.http.get<Imovel[]>(`${this.apiUrl}/buscar`, { params: { termo } });
  }

  buscarPorContribuinte(contribuinteId: string): Observable<Imovel[]> {
    return this.http.get<Imovel[]>(`${this.apiUrl}/por-contribuinte/${contribuinteId}`);
  }
}
