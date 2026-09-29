/// Parses `YYYY-MM` into a year and a zero-based month, as used by `Date`.
export function parseMonth(value: string): { year: number; month: number } {
  const [year, month] = value.split('-').map(Number);
  return { year, month: month - 1 };
}

/// Last day of the given zero-based month. Months beyond 11 roll over into the following years.
export function lastDayOfMonth(year: number, month: number): Date {
  // Day 0 of the next month is the last day of this month
  return new Date(year, month + 1, 0);
}

/// Debt is presented as a negative amount. Avoids showing `-0.00` once the debt is paid off.
export function asDebt(amount: number): number {
  return amount === 0 ? 0 : -amount;
}
