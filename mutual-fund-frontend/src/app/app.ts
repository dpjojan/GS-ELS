import { Component } from '@angular/core';
import { FundDashboard } from './components/fund-dashboard/fund-dashboard';

@Component({
  selector: 'app-root',
  imports: [FundDashboard],
  template: `<app-fund-dashboard />`,
  styles: []
})
export class App {}
