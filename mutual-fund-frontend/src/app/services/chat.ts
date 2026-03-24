import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class ChatService {
  private http = inject(HttpClient);
  private baseUrl = 'http://localhost:8080/api';

  sendMessage(message: string): Observable<string> {
    return this.http.post(`${this.baseUrl}/chat`, { message }, { responseType: 'text' });
  }
}
