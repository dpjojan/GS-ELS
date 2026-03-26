import { Component, OnInit, inject, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MutualFundService, MutualFundInfo } from '../../services/mutual-fund';

interface YearRow {
  year: number;
  futureValue: number;
  profit: number;
  profitPercent: number;
}

@Component({
  selector: 'app-fund-dashboard',
  imports: [CommonModule, FormsModule],
  template: `
    <div class="page">

      <!Header>
      <div class="header">
        <h1>Mutual Fund Calculator</h1>
        <p>Estimate the future value of your investment</p>
      </div>

      <div class="content">

        <!INPUT FORM>
        <div class="card">
          <h2>Enter Your Investment Details</h2>

          <!Fund Selector>
          <div class="field">
            <label>Select a Fund</label>
            <div class="fund-selector" [class.open]="dropdownOpen">
              <div class="fund-trigger" (click)="toggleDropdown()">
                <span [class.placeholder]="!selectedTicker">{{ selectedFundLabel }}</span>
                <span class="chevron">▾</span>
              </div>
              @if (dropdownOpen) {
                <div class="dropdown-backdrop" (click)="dropdownOpen = false; searchQuery = ''"></div>
                <div class="fund-dropdown">
                  <div class="fund-search-wrap">
                    <input
                      class="fund-search"
                      [(ngModel)]="searchQuery"
                      placeholder="Search by name or ticker..."
                      (click)="$event.stopPropagation()"
                    />
                  </div>
                  @if (!searchQuery.trim()) {
                    <div class="dropdown-group-label">Favorites</div>
                    @for (group of favoriteGroups; track group.label) {
                      @if (group.funds.length) {
                        <div class="dropdown-risk-label">{{ group.label }}</div>
                        @for (f of group.funds; track f.ticker) {
                          <div class="dropdown-item" [class.selected]="f.ticker === selectedTicker" (click)="selectFund(f.ticker)">
                            <span class="item-ticker">{{ f.ticker }}</span>
                            <span class="item-name">{{ f.name }}</span>
                          </div>
                        }
                      }
                    }
                  } @else {
                    @if (searchResults.length) {
                      <div class="dropdown-group-label">Search Results</div>
                      @for (f of searchResults; track f.ticker) {
                        <div class="dropdown-item" [class.selected]="f.ticker === selectedTicker" (click)="selectFund(f.ticker)">
                          <span class="item-ticker">{{ f.ticker }}</span>
                          <span class="item-name">{{ f.name }}</span>
                        </div>
                      }
                    } @else {
                      <div class="dropdown-empty">No funds found</div>
                    }
                  }
                </div>
              }
            </div>
            @if (selectedTicker) {
              <div class="risk-note" [ngClass]="riskClass">
                {{ riskLabel }} · {{ riskDescription }}
              </div>
            }
          </div>

          <!Principal>
          <div class="field">
            <label for="principal">How much are you investing today? ($)</label>
            <input
              id="principal"
              type="number"
              [(ngModel)]="principal"
              min="1"
              step="100"
              placeholder="e.g. 10000"
            />
          </div>

          <! Duration>
          <div class="field">
            <label for="years">How many years will you stay invested?</label>
            <select id="years" [(ngModel)]="years">
              @for (y of yearOptions; track y) {
                <option [value]="y">{{ y }} year{{ y > 1 ? 's' : '' }}</option>
              }
            </select>
          </div>

          <!Error>
          @if (errorMessage) {
            <div class="error-msg">{{ errorMessage }}</div>
          }

          <!Submit>
          <button
            class="calc-btn"
            (click)="calculate()"
            [disabled]="loading"
          >
            {{ loading ? 'Calculating...' : 'Calculate Future Value' }}
          </button>
        </div>

        <!RESULTS>
        @if (showResults) {
          <div class="card results-card">
            <h2>Results for {{ selectedTicker }}</h2>

            <!-- 3 big numbers -->
            <div class="summary-row">
              <div class="summary-box">
                <div class="summary-label">You invest</div>
                <div class="summary-value">{{ formatMoney(calculatedPrincipal) }}</div>
              </div>
              <div class="summary-box">
                <div class="summary-label">Estimated profit after {{ years }} year{{ years > 1 ? 's' : '' }}</div>
                <div class="summary-value green">+{{ formatMoney(finalProfit) }}</div>
              </div>
              <div class="summary-box">
                <div class="summary-label">Estimated future value</div>
                <div class="summary-value blue">{{ formatMoney(finalValue) }}</div>
              </div>
            </div>

            <p class="disclaimer">
              Based on historical 5-year average annual return (trading days only, holidays excluded).
              Total growth: +{{ totalGrowthPct.toFixed(1) }}%
            </p>

            <!Bar Chart (CSS/HTML) >
            <h3>Growth Year by Year</h3>
            <div class="chart">
              @for (row of yearRows; track row.year) {
                <div class="bar-group">
                  <div class="bar-labels">
                    <span class="bar-val-label">{{ (years <= 10 || row.year % 5 === 0) ? formatMoneyShort(row.futureValue) : '' }}</span>
                  </div>
                  <div class="bar-track">
                    <div
                      class="bar-invested"
                      [style.height.%]="(calculatedPrincipal / maxValue) * 100"
                    ></div>
                    <div
                      class="bar-profit"
                      [style.height.%]="(row.futureValue / maxValue) * 100"
                    ></div>
                  </div>
                  <div class="bar-year-label">Yr {{ row.year }}</div>
                </div>
              }
            </div>
            <div class="chart-legend">
              <span class="legend-dot blue-dot"></span> Future Value &nbsp;&nbsp;
              <span class="legend-dot green-dot"></span> Amount Invested
            </div>

            <!Table>
            <h3>Year-by-Year Breakdown</h3>
            <div class="table-wrap">
              <table>
                <thead>
                  <tr>
                    <th>Year</th>
                    <th>Amount Invested</th>
                    <th>Estimated Value</th>
                    <th>Estimated Profit</th>
                    <th>Total Growth</th>
                  </tr>
                </thead>
                <tbody>
                  @for (row of yearRows; track row.year) {
                    <tr [class.last-row]="row.year === years">
                      <td>{{ row.year }}</td>
                      <td>{{ formatMoney(calculatedPrincipal) }}</td>
                      <td class="green">{{ formatMoney(row.futureValue) }}</td>
                      <td class="green">+{{ formatMoney(row.profit) }}</td>
                      <td class="green">+{{ row.profitPercent.toFixed(1) }}%</td>
                    </tr>
                  }
                </tbody>
              </table>
            </div>
          </div>
        }

      </div>
    </div>
  `,
  styles: [`
    .page { background: #f5f5f5; min-height: 100vh; }

    .header {
      background: #1a3a6c;
      color: white;
      padding: 20px 24px;
    }
    .header h1 { font-size: 20px; font-weight: 700; margin-bottom: 4px; color: white}
    .header p { font-size: 13px; opacity: .7; }

    .content { max-width: 800px; margin: 0 auto; padding: 24px 20px; display: flex; flex-direction: column; gap: 20px; }

    .card {
      background: white;
      border: 1px solid #ddd;
      border-radius: 8px;
      padding: 24px;
    }
    .card h2 { font-size: 16px; font-weight: 700; margin-bottom: 20px; padding-bottom: 12px; border-bottom: 1px solid #eee; }
    .card h3 { font-size: 14px; font-weight: 700; margin: 20px 0 12px; color: #333; }

    .field { margin-bottom: 18px; }
    label { display: block; font-size: 12px; font-weight: 700; color: #555; text-transform: uppercase; letter-spacing: .04em; margin-bottom: 6px; }

    select, input[type=number] {
      width: 100%;
      padding: 10px 12px;
      border: 1px solid #ccc;
      border-radius: 6px;
      font-size: 14px;
      background: white;
      box-sizing: border-box;
    }
    select:focus, input:focus { outline: none; border-color: #1a3a6c; box-shadow: 0 0 0 3px rgba(26,58,108,.1); }

    .risk-note { font-size: 12px; margin-top: 6px; padding: 6px 10px; border-radius: 5px; }
    .risk-low { background: #e6f4ee; color: #166534; }
    .risk-med { background: #fef9c3; color: #854d0e; }
    .risk-high { background: #fee2e2; color: #991b1b; }

    .error-msg { background: #fee2e2; color: #991b1b; padding: 10px 14px; border-radius: 6px; font-size: 13px; margin-bottom: 14px; }

    .calc-btn {
      width: 100%;
      padding: 13px;
      background: #1a3a6c;
      color: white;
      border: none;
      border-radius: 6px;
      font-size: 15px;
      font-weight: 700;
      cursor: pointer;
    }
    .calc-btn:hover { background: #244f96; }
    .calc-btn:disabled { opacity: .6; cursor: not-allowed; }

    /* Fund Selector */
    .fund-selector { position: relative; }
    .fund-trigger {
      width: 100%; padding: 10px 12px;
      border: 1px solid #ccc; border-radius: 6px;
      font-size: 14px; background: white;
      cursor: pointer; display: flex; justify-content: space-between; align-items: center;
      user-select: none;
    }
    .fund-selector.open .fund-trigger { border-color: #1a3a6c; box-shadow: 0 0 0 3px rgba(26,58,108,.1); }
    .fund-trigger .placeholder { color: #999; }
    .chevron { color: #666; font-size: 12px; }
    .dropdown-backdrop { position: fixed; inset: 0; z-index: 10; }
    .fund-dropdown {
      position: absolute; top: calc(100% + 4px); left: 0; right: 0;
      background: white; border: 1px solid #ccc; border-radius: 6px;
      box-shadow: 0 4px 16px rgba(0,0,0,.12); z-index: 11;
      max-height: 300px; overflow-y: auto;
    }
    .fund-search-wrap { padding: 8px; border-bottom: 1px solid #eee; }
    .fund-search {
      width: 100%; padding: 8px 10px;
      border: 1px solid #ddd; border-radius: 5px;
      font-size: 13px; box-sizing: border-box;
    }
    .fund-search:focus { outline: none; border-color: #1a3a6c; }
    .dropdown-group-label { padding: 8px 12px 4px; font-size: 10px; font-weight: 700; color: #aaa; text-transform: uppercase; letter-spacing: .05em; }
    .dropdown-risk-label { padding: 4px 12px; font-size: 11px; color: #888; font-style: italic; }
    .dropdown-item { padding: 9px 12px; cursor: pointer; display: flex; gap: 8px; align-items: baseline; }
    .dropdown-item:hover { background: #f0f4f8; }
    .dropdown-item.selected { background: #e8f0fb; }
    .item-ticker { font-size: 13px; font-weight: 700; color: #1a3a6c; min-width: 52px; }
    .item-name { font-size: 12px; color: #555; }
    .dropdown-empty { padding: 16px 12px; font-size: 13px; color: #999; text-align: center; }

    /* Results */
    .results-card { }

    .summary-row { display: grid; grid-template-columns: repeat(3, 1fr); gap: 12px; margin-bottom: 14px; }
    .summary-box { background: #f7f7f7; border: 1px solid #e0e0e0; border-radius: 8px; padding: 16px; text-align: center; }
    .summary-label { font-size: 11px; font-weight: 700; color: #888; text-transform: uppercase; letter-spacing: .04em; margin-bottom: 6px; }
    .summary-value { font-size: 22px; font-weight: 800; color: #111; }
    .summary-value.green { color: #166534; }
    .summary-value.blue { color: #1a3a6c; }

    .disclaimer { font-size: 12px; color: #999; margin-bottom: 4px; }

    /* Chart */
    .chart {
      display: flex;
      align-items: flex-end;
      gap: 6px;
      height: 180px;
      padding: 0 4px;
      border-bottom: 2px solid #ddd;
      margin-bottom: 8px;
    }
    .bar-group { flex: 1; display: flex; flex-direction: column; align-items: center; height: 100%; }
    .bar-labels { font-size: 9px; color: #888; margin-bottom: 2px; min-height: 14px; text-align: center; }
    .bar-track { flex: 1; width: 100%; position: relative; display: flex; align-items: flex-end; }
    .bar-profit {
      position: absolute; bottom: 0; left: 0; right: 0;
      background: #1a3a6c; border-radius: 3px 3px 0 0;
      transition: height .5s ease;
    }
    .bar-invested {
      position: absolute; bottom: 0; left: 0; right: 0;
      background: #b8cce4;
      border-radius: 3px 3px 0 0;
      z-index: 1;
    }
    .bar-year-label { font-size: 9px; color: #aaa; margin-top: 4px; font-weight: 600; }
    .chart-legend { font-size: 12px; color: #555; margin-bottom: 4px; display: flex; align-items: center; gap: 4px; }
    .legend-dot { display: inline-block; width: 10px; height: 10px; border-radius: 2px; }
    .blue-dot { background: #1a3a6c; }
    .green-dot { background: #b8cce4; }

    /* Table */
    .table-wrap { overflow-x: auto; }
    table { width: 100%; border-collapse: collapse; font-size: 13px; }
    th { background: #f0f4f8; padding: 9px 12px; text-align: left; font-size: 11px; font-weight: 700; color: #555; text-transform: uppercase; letter-spacing: .04em; border-bottom: 2px solid #ddd; }
    td { padding: 10px 12px; border-bottom: 1px solid #f0f0f0; }
    tr.last-row td { font-weight: 700; background: #f7fbf7; }
    tr:hover td { background: #f7fbff; }
    td.green { color: #166534; font-weight: 600; }
  `]
})
export class FundDashboard implements OnInit {
  private service = inject(MutualFundService);
  private cdr = inject(ChangeDetectorRef);

  // Fund lists
  allFunds: MutualFundInfo[] = [];
  loadingFunds = true;

  // Grouped by risk (by ticker)
  lowRiskTickers  = ['FZILX', 'PRDGX'];
  medRiskTickers  = ['VFIAX', 'FXAIX'];
  highRiskTickers = ['VSMAX', 'SWLGX'];
  get lowRiskFunds()  { return this.allFunds.filter(f => this.lowRiskTickers.includes(f.ticker)); }
  get medRiskFunds()  { return this.allFunds.filter(f => this.medRiskTickers.includes(f.ticker)); }
  get highRiskFunds() { return this.allFunds.filter(f => this.highRiskTickers.includes(f.ticker)); }

  // Form inputs
  selectedTicker = '';
  principal = 10000;
  years = 5;
  yearOptions = [1, 2, 3, 5, 7, 10, 15, 20];

  // Dropdown state
  searchQuery = '';
  dropdownOpen = false;

  // State
  loading = false;
  errorMessage = '';
  showResults = false;

  // Results
  calculatedPrincipal = 0;
  finalValue = 0;
  finalProfit = 0;
  totalGrowthPct = 0;
  yearRows: YearRow[] = [];
  maxValue = 1;

  // Risk info for selected fund
  get selectedFundLabel(): string {
    if (!this.selectedTicker) return 'Choose a fund';
    const fund = this.allFunds.find(f => f.ticker === this.selectedTicker);
    return fund ? `${fund.ticker} — ${fund.name}` : this.selectedTicker;
  }

  get favoriteGroups() {
    return [
      { label: 'Low Risk (Bond Funds)',         funds: this.lowRiskFunds },
      { label: 'Medium Risk (S&P 500)',          funds: this.medRiskFunds },
      { label: 'High Risk (Growth / Small Cap)', funds: this.highRiskFunds },
    ];
  }

  get searchResults() {
    const q = this.searchQuery.trim().toLowerCase();
    if (!q) return [];
    return this.allFunds.filter(f =>
      f.ticker.toLowerCase().includes(q) || f.name.toLowerCase().includes(q)
    );
  }

  toggleDropdown() {
    this.dropdownOpen = !this.dropdownOpen;
    if (!this.dropdownOpen) this.searchQuery = '';
  }

  selectFund(ticker: string) {
    this.selectedTicker = ticker;
    this.dropdownOpen = false;
    this.searchQuery = '';
  }

  get riskLabel(): string {
    if (this.lowRiskTickers.includes(this.selectedTicker))  return 'Low Risk';
    if (this.medRiskTickers.includes(this.selectedTicker))  return 'Medium Risk';
    if (this.highRiskTickers.includes(this.selectedTicker)) return 'High Risk';
    return '';
  }
  get riskClass(): string {
    if (this.lowRiskTickers.includes(this.selectedTicker))  return 'risk-note risk-low';
    if (this.medRiskTickers.includes(this.selectedTicker))  return 'risk-note risk-med';
    if (this.highRiskTickers.includes(this.selectedTicker)) return 'risk-note risk-high';
    return 'risk-note';
  }
  get riskDescription(): string {
    if (this.lowRiskTickers.includes(this.selectedTicker))  return 'Bond fund. Steady, modest returns. Lower chance of loss.';
    if (this.medRiskTickers.includes(this.selectedTicker))  return 'S&P 500 index fund. Historically strong long-term returns.';
    if (this.highRiskTickers.includes(this.selectedTicker)) return 'Growth/small cap fund. Higher potential returns, more volatile.';
    return '';
  }

  searchQuery = '';
  favorites: string[] = JSON.parse(localStorage.getItem('favorites') || '[]');
  // search feature for mutual funds and favoriting
get filteredFunds(): MutualFundInfo[] {
  const query = this.searchQuery.toLowerCase();
  return this.allFunds.filter(f => f.name.toLowerCase().includes(query) || f.ticker.toLowerCase().includes(query));

  ngOnInit() {
    this.service.getFunds().subscribe({
      next: (funds) => {
        this.allFunds = funds;
        this.loadingFunds = false;
        this.cdr.detectChanges();
      },
      error: () => {
        // Fallback if backend not running
        this.allFunds = [
          { ticker: 'FZILX', name: 'Fidelity ZERO International Index Fund' },
          { ticker: 'PRDGX', name: 'T.Rowe Price Dividend Growth Fund' },
          { ticker: 'VFIAX', name: 'Vanguard 500 Index Fund' },
          { ticker: 'FXAIX', name: 'Fidelity 500 Index Fund' },
          { ticker: 'VSMAX', name: 'Vanguard Small Cap Index Fund' },
          { ticker: 'SWLGX', name: 'Schwab Large Cap Growth Fund' },
        ];
        this.loadingFunds = false;
        this.cdr.detectChanges();
      }
    });
  }

  calculate() {
    this.errorMessage = '';

    if (!this.selectedTicker) { this.errorMessage = 'Please select a fund.'; return; }
    if (!this.principal || this.principal <= 0) { this.errorMessage = 'Please enter a valid investment amount.'; return; }
    if (this.principal > 1_000_000_000) { this.errorMessage = 'Investment amount cannot exceed $1,000,000,000.'; return; }

    this.loading = true;
    this.showResults = false;
    this.calculatedPrincipal = this.principal;

    // The backend only returns the final future value for the full duration.
    // We call it once per year (year 1 through N) to build the chart.
    this.service.getFutureValueAllYears(this.selectedTicker, this.principal, this.years).subscribe({
      next: (values) => {
        this.yearRows = values.map((fv, i) => ({
          year: i + 1,
          futureValue: fv,
          profit: fv - this.calculatedPrincipal,
          profitPercent: ((fv - this.calculatedPrincipal) / this.calculatedPrincipal) * 100,
        }));

        this.finalValue = this.yearRows[this.yearRows.length - 1].futureValue;
        this.finalProfit = this.finalValue - this.calculatedPrincipal;
        this.totalGrowthPct = (this.finalProfit / this.calculatedPrincipal) * 100;
        this.maxValue = Math.max(...this.yearRows.map(r => r.futureValue));

        this.loading = false;
        this.showResults = true;
        this.cdr.detectChanges();
      },
      error: () => {
        this.errorMessage = 'Unable to calculate future value.';
        this.loading = false;
        this.cdr.detectChanges();
      }
    });
  }


  formatMoney(n: number): string {
    return '$' + n.toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
  }

  formatMoneyShort(n: number): string {
    if (n >= 1_000_000_000) return '$' + (n / 1_000_000_000).toFixed(1) + 'B';
    if (n >= 1_000_000)     return '$' + (n / 1_000_000).toFixed(1) + 'M';
    if (n >= 1_000)         return '$' + (n / 1_000).toFixed(1) + 'K';
    return '$' + Math.round(n);
  }
}

