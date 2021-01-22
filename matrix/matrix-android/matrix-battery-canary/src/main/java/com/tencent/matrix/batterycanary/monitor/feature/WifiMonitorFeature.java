package com.tencent.matrix.batterycanary.monitor.feature;

import com.tencent.matrix.batterycanary.utils.WifiManagerServiceHooker;
import com.tencent.matrix.util.MatrixLog;

public final class WifiMonitorFeature extends AbsMonitorFeature {
    private static final String TAG = "Matrix.battery.WifiMonitorFeature";
    final WifiCounting mCounting = new WifiCounting();
    WifiManagerServiceHooker.IListener mListener;

    @Override
    protected String getTag() {
        return TAG;
    }

    @Override
    public void onTurnOn() {
        super.onTurnOn();
        mListener = new WifiManagerServiceHooker.IListener() {
            @Override
            public void onStartScan() {
                MatrixLog.i(TAG, "#onStartScan");
                mCounting.onStartScan();
            }

            @Override
            public void onGetScanResults() {
                MatrixLog.i(TAG, "#onGetScanResults");
                mCounting.onGetScanResults();

            }
        };
        WifiManagerServiceHooker.addListener(mListener);
    }

    @Override
    public void onTurnOff() {
        super.onTurnOff();
        WifiManagerServiceHooker.removeListener(mListener);
        mCounting.onClear();
    }

    @Override
    public int weight() {
        return Integer.MIN_VALUE;
    }

    public WifiSnapshot currentSnapshot() {
        return mCounting.getSnapshot();
    }

    public static final class WifiCounting {
        private int mScanCount;
        private int mQueryCount;

        public void onStartScan() {
            mScanCount++;
        }

        public void onGetScanResults() {
            mQueryCount++;
        }

        public void onClear() {
            mScanCount = 0;
            mQueryCount = 0;
        }

        public WifiSnapshot getSnapshot() {
            WifiSnapshot snapshot = new WifiSnapshot();
            snapshot.scanCount = Snapshot.Entry.DigitEntry.of(mScanCount);
            snapshot.queryCount = Snapshot.Entry.DigitEntry.of(mQueryCount);
            snapshot.stack = "";
            return snapshot;
        }
    }

    public static class WifiSnapshot extends Snapshot<WifiSnapshot> {
        public Entry.DigitEntry<Integer> scanCount;
        public Entry.DigitEntry<Integer> queryCount;
        public String stack;

        @Override
        public Delta<WifiSnapshot> diff(WifiSnapshot bgn) {
            return new Delta<WifiSnapshot>(bgn, this) {
                @Override
                protected WifiSnapshot computeDelta() {
                    WifiSnapshot snapshot = new WifiSnapshot();
                    snapshot.scanCount = Differ.DigitDiffer.globalDiff(bgn.scanCount, end.scanCount);
                    snapshot.queryCount = Differ.DigitDiffer.globalDiff(bgn.queryCount, end.queryCount);
                    return snapshot;
                }
            };
        }
    }
}
