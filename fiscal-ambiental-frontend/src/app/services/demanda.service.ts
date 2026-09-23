import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Demanda, MinhaPontuacaoResposta, RankingFiscal, Usuario } from '../models/models';

@Injectable({
  providedIn: 'root'
})
export class DemandaService {
  private apiUrl = 'http://localhost:8080/api';

  constructor(private http: HttpClient) {}

  criarDemanda(demanda: Partial<Demanda>): Observable<Demanda> {
    return this.http.post<Demanda>(`${this.apiUrl}/gestor/demandas`, demanda);
  }

  editarDemanda(demandaId: string, demanda: Partial<Demanda>): Observable<Demanda> {
    return this.http.put<Demanda>(`${this.apiUrl}/gestor/demandas/${demandaId}`, demanda);
  }

  excluirDemanda(demandaId: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/gestor/demandas/${demandaId}`);
  }

  delegarDemanda(demandaId: string, fiscalId: string): Observable<Demanda> {
    return this.http.put<Demanda>(`${this.apiUrl}/gestor/demandas/${demandaId}/delegar/${fiscalId}`, {});
  }

  listarTodas(): Observable<Demanda[]> {
    return this.http.get<Demanda[]>(`${this.apiUrl}/gestor/demandas`);
  }

  listarMinhasDemandas(): Observable<Demanda[]> {
    return this.http.get<Demanda[]>(`${this.apiUrl}/fiscal/demandas`);
  }

  finalizarDemanda(demandaId: string): Observable<Demanda> {
    return this.http.post<Demanda>(`${this.apiUrl}/fiscal/demandas/${demandaId}/finalizar`, {});
  }

  minhaPontuacao(): Observable<MinhaPontuacaoResposta> {
    return this.http.get<MinhaPontuacaoResposta>(`${this.apiUrl}/fiscal/minha-pontuacao`);
  }

  listarFiscais(): Observable<Usuario[]> {
    return this.http.get<Usuario[]>(`${this.apiUrl}/gestor/fiscais`);
  }

  rankingFiscais(): Observable<RankingFiscal[]> {
    return this.http.get<RankingFiscal[]>(`${this.apiUrl}/gestor/ranking-fiscais`);
  }
}
