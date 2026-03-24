import { Component } from '@angular/core';
import { FundDashboard } from './components/fund-dashboard/fund-dashboard';
import { ChatbotComponent } from './components/chatbot/chatbot';

@Component({
  selector: 'app-root',
  imports: [FundDashboard, ChatbotComponent],
  template: `
    <app-fund-dashboard />
    <app-chatbot />
  `,
  styles: []
})
export class App {}
