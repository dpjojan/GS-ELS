import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface MutualFundInfo {
  ticker: string;
  name: string;
}

@Injectable({ providedIn: 'root' })
export class MutualFundService {
  private http = inject(HttpClient);
  private baseUrl = 'http://localhost:8080/api';

  getFunds(): Observable<MutualFundInfo[]> {
    return this.http.get<MutualFundInfo[]>(`${this.baseUrl}/funds`);
  }

  getFutureValueAllYears(ticker: string, principal: number, years: number): Observable<number[]> {
    return this.http.get<number[]>(`${this.baseUrl}/futureValAll`, {
      params: { ticker, principal, years }
    });
  }
}
