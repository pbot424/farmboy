package com.dailytree;

import org.junit.Test;
import static org.junit.Assert.*;

public class OverlayRenderTest
{
    private enum PatchState
    {
        EMPTY,
        GROWING,
        READY,
        DISEASED
    }

    private static class PatchOverlay
    {
        private boolean showOverlay = true;

        void setShowOverlay(boolean show)
        {
            this.showOverlay = show;
        }

        String render(PatchState state)
        {
            if (!showOverlay)
            {
                return null;
            }

            switch (state)
            {
                case READY:
                    return "green";
                case GROWING:
                    return "yellow";
                case DISEASED:
                    return "red";
                case EMPTY:
                default:
                    return "gray";
            }
        }
    }

    @Test
    public void overlayColorsMatchPatchState()
    {
        PatchOverlay overlay = new PatchOverlay();
        assertEquals("gray", overlay.render(PatchState.EMPTY));
        assertEquals("yellow", overlay.render(PatchState.GROWING));
        assertEquals("green", overlay.render(PatchState.READY));
        assertEquals("red", overlay.render(PatchState.DISEASED));
    }

    @Test
    public void overlayRespectsConfigurationToggle()
    {
        PatchOverlay overlay = new PatchOverlay();
        overlay.setShowOverlay(false);
        assertNull("overlay should not render when disabled", overlay.render(PatchState.READY));
    }
}

