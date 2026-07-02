import { HttpClient, HttpParams } from "@angular/common/http";
import { Injectable, inject } from "@angular/core";
import { environment } from "../environments/environment";
import { ExchangeRate } from "../models/exchange-rate";
import { Observable } from "rxjs";

@Injectable({
  providedIn: 'root'
})
export class ExchangeRateService {
  private readonly http =
    inject(HttpClient);
  private readonly api =
    `${environment.apiUrl}/exchange-rates`;
  findAll() {
    return this.http.get<ExchangeRate[]>(this.api);
  }

  search(from: string, to: string): Observable<ExchangeRate> {
    const params = new HttpParams()
      .set('fromCurrencyCode', from)
      .set('toCurrencyCode', to);
    return this.http.get<ExchangeRate>(
      `${this.api}/search`,
      { params }
    );

  }
}
