export interface ReceivableSimulationRequest {
  sellerName: string;
  faceValue: number;
  dueDate: string;
  currencyCode: string;
  paymentCurrencyCode: string;
  receivableType: string;
}
