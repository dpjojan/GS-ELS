import { Component, inject, ElementRef, ViewChild, AfterViewChecked, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ChatService } from '../../services/chat';

interface Message {
  role: 'user' | 'bot';
  text: string;
}

@Component({
  selector: 'app-chatbot',
  imports: [CommonModule, FormsModule],
  template: `
    <!-- Toggle bubble -->
    <button class="chat-toggle" (click)="isOpen = !isOpen">
      {{ isOpen ? '✕' : '💬' }}
    </button>

    <!-- Chat panel -->
    @if (isOpen) {
      <div class="chat-panel">
        <div class="chat-header">
          <span>Fund Assistant</span>
          <small>Powered by Gemini</small>
        </div>

        <div class="chat-messages" #scrollContainer>
          @if (messages.length === 0) {
            <div class="empty-state">Ask me anything about mutual funds!</div>
          }
          @for (msg of messages; track $index) {
            <div class="message" [class.user]="msg.role === 'user'" [class.bot]="msg.role === 'bot'">
              <div class="bubble">{{ msg.text }}</div>
            </div>
          }
          @if (loading) {
            <div class="message bot">
              <div class="bubble typing">Thinking...</div>
            </div>
          }
        </div>

        <div class="chat-input-row">
          <input
            type="text"
            [(ngModel)]="input"
            (keydown.enter)="send()"
            placeholder="Ask about mutual funds..."
            [disabled]="loading"
          />
          <button (click)="send()" [disabled]="loading || !input.trim()">Send</button>
        </div>
      </div>
    }
  `,
  styles: [`
    .chat-toggle {
      position: fixed;
      bottom: 24px;
      right: 24px;
      width: 52px;
      height: 52px;
      border-radius: 50%;
      background: #1a3a6c;
      color: white;
      border: none;
      font-size: 22px;
      cursor: pointer;
      box-shadow: 0 4px 12px rgba(0,0,0,0.2);
      z-index: 1000;
    }
    .chat-toggle:hover { background: #244f96; }

    .chat-panel {
      position: fixed;
      bottom: 88px;
      right: 24px;
      width: 340px;
      height: 460px;
      background: white;
      border: 1px solid #ddd;
      border-radius: 12px;
      box-shadow: 0 8px 24px rgba(0,0,0,0.15);
      display: flex;
      flex-direction: column;
      z-index: 999;
    }

    .chat-header {
      background: #1a3a6c;
      color: white;
      padding: 14px 16px;
      border-radius: 12px 12px 0 0;
      display: flex;
      justify-content: space-between;
      align-items: center;
      font-weight: 700;
      font-size: 14px;
    }
    .chat-header small { font-size: 11px; opacity: 0.7; font-weight: 400; }

    .chat-messages {
      flex: 1;
      overflow-y: auto;
      padding: 14px;
      display: flex;
      flex-direction: column;
      gap: 10px;
    }

    .empty-state {
      text-align: center;
      color: #aaa;
      font-size: 13px;
      margin-top: 40px;
    }

    .message { display: flex; }
    .message.user { justify-content: flex-end; }
    .message.bot  { justify-content: flex-start; }

    .bubble {
      max-width: 80%;
      padding: 9px 13px;
      border-radius: 16px;
      font-size: 13px;
      line-height: 1.5;
    }
    .message.user .bubble {
      background: #1a3a6c;
      color: white;
      border-bottom-right-radius: 4px;
    }
    .message.bot .bubble {
      background: #f0f4f8;
      color: #222;
      border-bottom-left-radius: 4px;
    }
    .bubble.typing { color: #999; font-style: italic; }

    .chat-input-row {
      display: flex;
      gap: 8px;
      padding: 12px;
      border-top: 1px solid #eee;
    }
    .chat-input-row input {
      flex: 1;
      padding: 9px 12px;
      border: 1px solid #ccc;
      border-radius: 6px;
      font-size: 13px;
    }
    .chat-input-row input:focus { outline: none; border-color: #1a3a6c; }
    .chat-input-row button {
      padding: 9px 14px;
      background: #1a3a6c;
      color: white;
      border: none;
      border-radius: 6px;
      font-size: 13px;
      font-weight: 700;
      cursor: pointer;
    }
    .chat-input-row button:hover { background: #244f96; }
    .chat-input-row button:disabled { opacity: 0.5; cursor: not-allowed; }
  `]
})
export class ChatbotComponent implements AfterViewChecked {
  private chatService = inject(ChatService);
  private cdr = inject(ChangeDetectorRef);

  @ViewChild('scrollContainer') private scrollContainer!: ElementRef;

  isOpen = false;
  input = '';
  loading = false;
  messages: Message[] = [];

  ngAfterViewChecked() {
    this.scrollToBottom();
  }

  send() {
    const text = this.input.trim();
    if (!text || this.loading) return;

    this.messages.push({ role: 'user', text });
    this.input = '';
    this.loading = true;

    this.chatService.sendMessage(text).subscribe({
      next: (response) => {
        this.messages.push({ role: 'bot', text: response });
        this.loading = false;
        this.cdr.detectChanges();
      },
      error: () => {
        this.messages.push({ role: 'bot', text: 'Sorry, something went wrong. Please try again.' });
        this.loading = false;
        this.cdr.detectChanges();
      }
    });
  }

  private scrollToBottom() {
    try {
      this.scrollContainer.nativeElement.scrollTop = this.scrollContainer.nativeElement.scrollHeight;
    } catch {}
  }
}
