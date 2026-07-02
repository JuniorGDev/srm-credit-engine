import { Component, computed, inject, signal } from '@angular/core';
import {
  FormBuilder,
  ReactiveFormsModule,
  Validators
} from '@angular/forms';

import { SimulationService } from '../../core/services/simulation.service';
import { ReceivableSimulationResponse } from '../../core/models/receivable-simulation-response';
import { CurrencyPipe } from '@angular/common';
import { InputTextModule } from 'primeng/inputtext';
import { SelectModule } from 'primeng/select';
import { ButtonModule } from 'primeng/button';
import { CardModule } from 'primeng/card';
import { DatePickerModule } from 'primeng/datepicker';
import { InputNumberModule } from 'primeng/inputnumber';
import { Currency, CurrencyService } from '../../core/services/currency.service';
import { SettlementService } from '../../core/services/settlement.service';
import { ExchangeRate } from '../../core/models/exchange-rate';
import { ExchangeRateService } from '../../core/services/exchange-rate.service';
import { combineLatest, startWith } from 'rxjs';
import { SkeletonModule } from 'primeng/skeleton';
import { NotificationService } from '../../core/services/notification.service';

@Component({
  selector: 'app-simulation',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    InputTextModule,
    InputNumberModule,
    SelectModule,
    DatePickerModule,
    ButtonModule,
    CardModule,
    CurrencyPipe,
    SkeletonModule
  ],
  templateUrl: './simulation.html',
  styleUrl: './simulation.scss'
})
export class Simulation {

  private readonly fb = inject(FormBuilder);

  private readonly settlementService = inject(SettlementService);
  private readonly simulationService = inject(SimulationService);
  private readonly currencyService = inject(CurrencyService);
  private readonly exchangeRateService = inject(ExchangeRateService);
  private readonly notificationService = inject(NotificationService);

  readonly currencies = signal<Currency[]>([]);

  readonly result = signal<ReceivableSimulationResponse | null>(null);
  readonly exchangeRate = signal<ExchangeRate | null>(null);

  readonly loading = signal(false);

  readonly form = this.fb.group({
    sellerName: ['', Validators.required],
    receivableType: ['', Validators.required],
    faceValue: [null, Validators.required],
    dueDate: ['', Validators.required],
    currencyCode: ['BRL', Validators.required],
    paymentCurrencyCode: ['BRL', Validators.required]
  });

  ngOnInit(): void {
    this.loadCurrencies();
    this.watchExchangeRate();
  }

  private loadCurrencies() {
    this.currencyService.findAll().subscribe({
      next: currencies => this.currencies.set(currencies)
    });
  }

  private watchExchangeRate(): void {
    const fromControl = this.form.controls.currencyCode;
    const toControl = this.form.controls.paymentCurrencyCode;

    combineLatest([
      fromControl.valueChanges.pipe(startWith(fromControl.value)),
      toControl.valueChanges.pipe(startWith(toControl.value))
    ]).subscribe(([from, to]) => {

      if (!from || !to) {
        return;
      }

      if (from === to) {
        this.exchangeRate.set({
          id: 0,
          fromCurrency: from,
          toCurrency: to,
          rate: 1
        });
        return;
      }
      this.loadExchangeRate(from, to);
    });
  }

  private loadExchangeRate(
    from: string,
    to: string
  ): void {
    this.exchangeRateService
      .search(from, to)
      .subscribe({
        next: rate => {
          this.exchangeRate.set(rate);
        },
        error: () => {
          this.exchangeRate.set(null);
        }
      });
  }

  simulate(): void {
    if (this.form.invalid) {
      return;
    }
    this.loading.set(true);
    this.simulationService
      .simulate(this.form.getRawValue() as any)
      .subscribe({
        next: response => {
          this.result.set(response);
          this.loading.set(false);
        },
        error: error => {
          console.error(error);
          this.loading.set(false);
        }
      });
  }

  confirmSettlement(): void {
    if (this.form.invalid) {
      return;
    }
    this.loading.set(true);
    this.settlementService
      .create(this.form.getRawValue() as any)
      .subscribe({
        next: () => {
          this.loading.set(false);
          this.notificationService.success('Settlement criado com sucesso.', 'Sucesso');
          this.result.set(null);
          this.form.reset();
        },
        error: () => {
          this.loading.set(false);
          this.notificationService.error('Erro ao criar settlement.', 'Erro');
        }
      });
  }

}
