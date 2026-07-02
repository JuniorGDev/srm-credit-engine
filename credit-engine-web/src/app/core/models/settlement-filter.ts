export interface SettlementFilter {
  sellerName?: string;
  currencyCode?: string;
  startDate?: string;
  endDate?: string;
  page: number;
  size: number;
}
