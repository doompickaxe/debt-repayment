import { CurrencyPipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, input } from '@angular/core';
import { RepaymentSummary } from '../api';
import { asDebt } from './repayment-dates';

@Component({
  selector: 'app-repayment-summary',
  imports: [CurrencyPipe],
  template: `
    <dl class="grid grid-cols-2 gap-4 lg:grid-cols-4">
      @for (item of items(); track item.label) {
        <div class="bg-card rounded-lg border p-4">
          <dt class="text-muted-foreground text-sm">{{ item.label }}</dt>
          <dd class="mt-1 text-lg font-semibold tabular-nums">{{ item.value | currency: 'EUR' }}</dd>
        </div>
      }
    </dl>
  `,
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class RepaymentSummaryView {
  readonly summary = input.required<RepaymentSummary>();

  protected items() {
    const s = this.summary();
    return [
      { label: 'Remaining debt', value: asDebt(s.remainingDebt) },
      { label: 'Total interest', value: s.totalInterest },
      { label: 'Total repayment', value: s.totalRepayment },
      { label: 'Total instalments', value: s.totalInstalments },
    ];
  }
}
