import yfinance as yf
import json
import sys
from datetime import date
from dateutil.relativedelta import relativedelta

SYMBOL = sys.argv[1] if len(sys.argv) > 1 else "VBTLX"
NUM_YEARS = 5

today = date.today()
ticker = yf.Ticker(SYMBOL)
results = []

for i in range(NUM_YEARS, 0, -1):
    start = today - relativedelta(years=i)
    end   = today - relativedelta(years=i - 1)

    history = ticker.history(start=start.strftime("%Y-%m-%d"),
                             end=end.strftime("%Y-%m-%d"),
                             interval="1d",
                             auto_adjust=False)

    if history.empty:
        continue

    oldest = history.iloc[0]
    newest = history.iloc[-1]
    change = (newest["Close"] - oldest["Close"]) / oldest["Close"] * 100

    results.append({
        "period":       f"{start.year}-{end.year}",
        "startDate":    str(history.index[0].date()),
        "endDate":      str(history.index[-1].date()),
        "startClose":   round(float(oldest["Close"]), 2),
        "endClose":     round(float(newest["Close"]), 2),
        "changePercent": round(change, 2)
    })

average = round(sum(r["changePercent"] for r in results) / len(results), 2) if results else 0.0

print(json.dumps({"periods": results, "averageChangePercent": average}))
