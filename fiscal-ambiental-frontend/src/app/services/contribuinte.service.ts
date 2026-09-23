import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Contribuinte } from '../models/models';

@Injectable({
  providedIn: 'root'
})
export class ContribuinteService {
  private apiUrl = 'http://localhost:8080/api/contribuintes';

  constructor(private http: HttpClient) {}

  buscar(termo: string): Observable<Contribuinte[]> {
    return this.http.get<Contribuinte[]>(`${this.apiUrl}/buscar`, { params: { termo } });
  }
}
