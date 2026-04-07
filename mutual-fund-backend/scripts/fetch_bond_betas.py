import yfinance as yf
import pandas as pd
import pandas_datareader.data as web
import statsmodels.api as sm
import json
import sys
import os
from datetime import date
from dateutil.relativedelta import relativedelta

SYMBOL = sys.argv[1] if len(sys.argv) > 1 else "BND"
NUM_YEARS = 5

today = date.today()
start = today - relativedelta(years=NUM_YEARS)

# Fetch fund monthly returns
ticker = yf.Ticker(SYMBOL)
history = ticker.history(start=start, end=today, interval="1mo", auto_adjust=True)
fund_returns = history["Close"].pct_change().dropna()
fund_returns.index = fund_returns.index.to_period("M")

# Fetch daily yield series from FRED
dgs10  = web.DataReader("DGS10",  "fred", start, today)  # 10-year Treasury
dgs3mo = web.DataReader("DGS3MO", "fred", start, today)  # 3-month T-bill
baa    = web.DataReader("BAA",    "fred", start, today)  # Moody's BAA corporate

# Average daily values down to one value per month
dgs10_m  = dgs10.resample("ME").mean()
dgs3mo_m = dgs3mo.resample("ME").mean()
baa_m    = baa.resample("ME").mean()

# Build the two factors (convert % to decimal)
term_factor   = (dgs10_m["DGS10"] - dgs3mo_m["DGS3MO"]) / 100  # long vs short rate spread
credit_factor = (baa_m["BAA"]     - dgs10_m["DGS10"])   / 100  # corporate vs treasury spread

term_factor.index   = term_factor.index.to_period("M")
credit_factor.index = credit_factor.index.to_period("M")

# Align fund returns and factors by month
df = pd.concat([fund_returns, term_factor, credit_factor], axis=1).dropna()
df.columns = ["fund_return", "term_factor", "credit_factor"]

# Run regression: Ri = alpha + beta1*Term + beta2*Credit
X     = sm.add_constant(df[["term_factor", "credit_factor"]])
model = sm.OLS(df["fund_return"], X).fit()

print(json.dumps({
    "ticker": SYMBOL,
    "alpha":  round(float(model.params["const"]),         6),
    "beta1":  round(float(model.params["term_factor"]),   6),
    "beta2":  round(float(model.params["credit_factor"]), 6)
}))
