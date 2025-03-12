package org.autojs.autoxjs.external.tile;

import android.os.Build;

import androidx.annotation.RequiresApi;

import com.stardust.view.accessibility.NodeInfo;

import org.autojs.autoxjs.ui.floating.FullScreenFloatyWindow;
import org.autojs.autoxjs.ui.floating.layoutinspector.LayoutBoundsFloatyWindow;

@RequiresApi(api = Build.VERSION_CODES.N)
public class LayoutBoundsTile extends LayoutInspectTileService {
    @Override
    protected FullScreenFloatyWindow onCreateWindow(NodeInfo capture) {
        return new LayoutBoundsFloatyWindow(capture) {
            @Override
            public void close() {
                super.close();
                inactive();
            }
        };
    }
}
