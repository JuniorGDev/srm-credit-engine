import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';

import { Observable } from 'rxjs';

import { ReceivableSimulationRequest } from '../models/receivable-simulation-request';
import { ReceivableSimulationResponse } from '../models/receivable-simulation-response';
import { environment } from '../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class SimulationService {

  private readonly http = inject(HttpClient);

  private readonly api = environment.apiUrl + '/settlements';

  simulate(
    request: ReceivableSimulationRequest
  ): Observable<ReceivableSimulationResponse> {

    return this.http.post<ReceivableSimulationResponse>(
      `${this.api}/simulate`,
      request
    );

  }

}
