export interface SettlementStatement {
  id: number;
  sellerName: string;
  receivableType: string;
  faceValue: number;
  receivableCurrency: string;
  paymentCurrency: string;
  exchangeRate: number;
  presentValue: number;
  discountValue: number;
  paymentAmount: number;
  dueDate: string;
  createdAt: string;
}
