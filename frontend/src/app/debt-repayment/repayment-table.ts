import { CurrencyPipe, DatePipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, computed, input } from '@angular/core';
import { HlmTableImports } from '@spartan-ng/helm/table';
import { RepaymentScheduleEntry } from '../api';
import { asDebt, lastDayOfMonth, parseMonth } from './repayment-dates';

interface TableRow {
  date: Date;
  remainingDebt: number;
  interest?: number;
  repayment?: number;
  instalment?: number;
}

/// Repayment schedule, starting with the payout on the 1st of the payout month, followed by one row per instalment
/// on the last day of each month. Debt is shown as a negative number.
/// The table scrolls inside its container and keeps the header visible.
@Component({
  selector: 'app-repayment-table',
  imports: [CurrencyPipe, DatePipe, HlmTableImports],
  template: `
    <div hlmTableContainer class="max-h-[60vh] overflow-y-auto rounded-lg border" tabindex="0" aria-label="Repayment schedule">
      <table hlmTable>
        <thead hlmTHead class="bg-muted sticky top-0 z-10">
          <tr hlmTr>
            <th hlmTh class="bg-muted sticky left-0">Date</th>
            <th hlmTh class="text-right">Remaining debt</th>
            <th hlmTh class="text-right">Interest</th>
            <th hlmTh class="text-right">Repayment</th>
            <th hlmTh class="text-right">Instalment</th>
          </tr>
        </thead>
        <tbody hlmTBody class="tabular-nums">
          @for (row of rows(); track row.date.getTime()) {
            <tr hlmTr>
              <td hlmTd class="bg-card sticky left-0">{{ row.date | date: 'mediumDate' }}</td>
              <td hlmTd class="text-right">{{ row.remainingDebt | currency: 'EUR' }}</td>
              <td hlmTd class="text-right">{{ row.interest === undefined ? '–' : (row.interest | currency: 'EUR') }}</td>
              <td hlmTd class="text-right">{{ row.repayment === undefined ? '–' : (row.repayment | currency: 'EUR') }}</td>
              <td hlmTd class="text-right">{{ row.instalment === undefined ? '–' : (row.instalment | currency: 'EUR') }}</td>
            </tr>
          }
        </tbody>
      </table>
    </div>
  `,
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class RepaymentTable {
  readonly payout = input.required<number>();
  /// Month of the payout as `YYYY-MM`
  readonly payoutMonth = input.required<string>();
  readonly schedule = input.required<RepaymentScheduleEntry[]>();

  protected readonly rows = computed<TableRow[]>(() => {
    const { year, month } = parseMonth(this.payoutMonth());
    const payoutRow: TableRow = { date: new Date(year, month, 1), remainingDebt: asDebt(this.payout()) };

    return [
      payoutRow,
      ...this.schedule().map((entry) => ({
        date: lastDayOfMonth(year, month + entry.month - 1),
        remainingDebt: asDebt(entry.remainingDebt),
        interest: entry.interest,
        repayment: entry.repayment,
        instalment: entry.instalment,
      })),
    ];
  });
}
