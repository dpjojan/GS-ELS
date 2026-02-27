import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root',
})

export class MutualFund {
  private apiUrl = 'http://localhost:4200'; 

  constructor(private http: HttpClient) {}
  getMutualFunds(): Observable<any[]> {
    return this.http.get<any[]>(this.apiUrl);
}
