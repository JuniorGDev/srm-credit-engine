import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';

import { catchError, throwError } from 'rxjs';
import { NotificationService } from '../services/notification.service';

export const errorInterceptor: HttpInterceptorFn = (req, next) => {

  const notificationService = inject(NotificationService);
  return next(req).pipe(

    catchError((error: HttpErrorResponse) => {

      console.error(error);

      let message = 'Erro inesperado.';

      switch (error.status) {

        case 400:
          message = 'Dados inválidos.';
          break;

        case 404:
          message = 'Registro não encontrado.';
          break;

        case 500:
          message = 'Erro interno do servidor.';
          break;

      }

      if (error.error && error.error.detail) {
        message = error.error.detail;
      }

      notificationService.error(message, 'Erro');

      return throwError(() => error);

    })

  );

};
