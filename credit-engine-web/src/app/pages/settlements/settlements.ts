
import { Component, inject, signal } from '@angular/core';
import { FormBuilder, FormsModule, ReactiveFormsModule } from '@angular/forms';
import { CurrencyPipe, DatePipe } from '@angular/common';

import { SettlementService } from '../../core/services/settlement.service';
import { SettlementStatement } from '../../core/models/settlement-statement';
import { ExchangeRateService } from '../../core/services/exchange-rate.service';
import { Currency, CurrencyService } from '../../core/services/currency.service';
import { ExchangeRate } from '../../core/models/exchange-rate';

import { TableLazyLoadEvent, TableModule } from 'primeng/table';
import { ButtonModule } from 'primeng/button';
import { InputTextModule } from 'primeng/inputtext';
import { SelectModule } from 'primeng/select';
import { CardModule } from 'primeng/card';
import { DatePickerModule } from 'primeng/datepicker';
import { SkeletonModule } from 'primeng/skeleton';
import { finalize, first } from 'rxjs';
@Component({
  selector: 'app-settlements',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    FormsModule,
    CurrencyPipe,
    DatePickerModule,
    DatePipe,
    TableModule,
    ButtonModule,
    InputTextModule,
    SelectModule,
    CardModule,
    SkeletonModule
  ],
  templateUrl: './settlements.html',
  styleUrl: './settlements.scss'
})
export class Settlements {

  private readonly fb = inject(FormBuilder);

  private readonly settlementService = inject(SettlementService);
  private readonly currencyService = inject(CurrencyService);
  private readonly exchangeRateService = inject(ExchangeRateService);
  readonly currencies = signal<Currency[]>([]);
  readonly exchangeRates = signal<ExchangeRate[]>([]);
  readonly settlements = signal<SettlementStatement[]>([]);
  readonly filterForm = this.fb.group({
    sellerName: [''],
    currencyCode: [''],
    startDate: new Date(),
    endDate: new Date()
  });
  readonly loading = signal(false);
  readonly page = signal(0);
  readonly size = signal(10);
  readonly totalRecords = signal(0);

  ngOnInit() {
    this.load();
    this.loadCurrency();
  }

  filter() {
    this.page.set(0);
    this.load();
  }

  load() {
    this.loading.set(true);
    const filter = this.filterForm.value;
    this.settlementService
      .statement({
        sellerName: filter.sellerName ?? '',
        currencyCode: filter.currencyCode ?? '',
        startDate: this.formatDate(filter.startDate ?? new Date()),
        endDate: this.formatDate(filter.endDate ?? new Date()),
        page: this.page(),
        size: this.size()
      })
      .pipe(
        first(),
        finalize(() => this.loading.set(false))
      ).subscribe(response => {
        this.settlements.set(response.content);
        this.totalRecords.set(response.totalElements);
      });
  }

  loadCurrency() {
    this.currencyService.findAll().subscribe({
      next: currencies => this.currencies.set(currencies)
    });
    this.exchangeRateService
      .findAll()
      .subscribe(r => {
        this.exchangeRates.set(r);
      });
  }

  formatDate(date: Date): string {
    if (!date) {
      return '';
    }
    const year = date.getFullYear();
    const month = (date.getMonth() + 1).toString().padStart(2, '0');
    const day = date.getDate().toString().padStart(2, '0');
    return `${year}-${month}-${day}`;
  }

  onLazyLoad(event: TableLazyLoadEvent): void {
    this.page.set((event.first ?? 0) / (event.rows ?? 10));
    this.size.set(event.rows ?? 10);
    this.load();
  }

}
