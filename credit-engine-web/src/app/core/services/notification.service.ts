import { inject, Injectable } from '@angular/core';
import { MessageService } from 'primeng/api';

@Injectable({
  providedIn: 'root'
})
export class NotificationService {

  private readonly messageService = inject(MessageService);

  success(detail: string, summary = 'Sucesso'): void {
    this.messageService.add({
      severity: 'success',
      summary,
      detail
    });
  }

  error(detail: string, summary = 'Erro'): void {
    this.messageService.add({
      severity: 'error',
      summary,
      detail
    });
  }

  warning(detail: string, summary = 'Atenção'): void {
    this.messageService.add({
      severity: 'warn',
      summary,
      detail
    });
  }

  info(detail: string, summary = 'Informação'): void {
    this.messageService.add({
      severity: 'info',
      summary,
      detail
    });
  }

}
