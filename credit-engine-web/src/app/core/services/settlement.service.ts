import { inject, Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';

import { Observable } from 'rxjs';

import { PageResponse } from '../models/page-response';
import { SettlementStatement } from '../models/settlement-statement';
import { environment } from '../environments/environment';
import { ReceivableSimulationRequest } from '../models/receivable-simulation-request';
import { SettlementFilter } from '../models/settlement-filter';

@Injectable({
  providedIn: 'root'
})
export class SettlementService {

  private readonly http = inject(HttpClient);

  private readonly api = environment.apiUrl + '/settlements';

  statement(filter: SettlementFilter): Observable<PageResponse<SettlementStatement>> {
    let params = new HttpParams();

    if (filter.sellerName) {
        params = params.set('sellerName', filter.sellerName);
    }

    if (filter.currencyCode) {
        params = params.set('currencyCode', filter.currencyCode);
    }

    if (filter.startDate) {
        params = params.set('startDate', filter.startDate);
    }

    if (filter.endDate) {
        params = params.set('endDate', filter.endDate);
    }

    params = params
        .set('page', filter.page)
        .set('size', filter.size);

    return this.http.get<PageResponse<SettlementStatement>>(
        `${this.api}/statement`,
        { params }
    );

  }

  create(
    request: ReceivableSimulationRequest
) {

    return this.http.post(
        this.api,
        request
    );

}

}
