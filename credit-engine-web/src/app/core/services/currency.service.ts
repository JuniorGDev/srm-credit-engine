import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../environments/environment';

export interface Currency {
  id: number;
  code: string;
  name: string;
}

@Injectable({
  providedIn: 'root'
})
export class CurrencyService {

  private readonly http = inject(HttpClient);

  private readonly api =  environment.apiUrl + '/currencies';

  findAll(): Observable<Currency[]> {
    return this.http.get<Currency[]>(this.api);
  }
}
