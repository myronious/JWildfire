/*
  JWildfire - an image and animation processor written in Java 
  Copyright (C) 1995-2015 Andreas Maschke

  This is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser 
  General Public License as published by the Free Software Foundation; either version 2.1 of the 
  License, or (at your option) any later version.
 
  This software is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without 
  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU 
  Lesser General Public License for more details.

  You should have received a copy of the GNU Lesser General Public License along with this software; 
  if not, write to the Free Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
  02110-1301 USA, or see the FSF site: http://www.fsf.org.
*/
package org.jwildfire.create.tina;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;

import javax.swing.*;

import org.jwildfire.base.Prefs;
import org.jwildfire.create.tina.swing.*;
import org.jwildfire.create.tina.swing.flamepanel.FlamePanel;
import org.jwildfire.create.tina.swing.flamepanel.FlamePanelConfig;
import org.jwildfire.image.SimpleImage;
import org.jwildfire.swing.ErrorHandler;
import org.jwildfire.transform.ComposeTransformer;

public class AnimationController {
  private final TinaController tinaController;
  private final ErrorHandler errorHandler;
  private final Prefs prefs;
  private final JPanel rootPanel;
  private final JToggleButton motionCurveEditModeButton;
  private final JWFNumberField keyframesFrameField;
  private final JSlider keyframesFrameSlider;
  private final JWFNumberField keyframesFrameCountField;
  private final List<MotionCurveEditor> motionPropertyControls = new ArrayList<MotionCurveEditor>();
  private final JPanel frameSliderPanel;
  private final JLabel keyframesFrameLbl;
  private final JLabel keyframesFrameCountLbl;
  private final JPanel motionBlurPanel;
  private final JButton motionCurvePlayPreviewButton;
  private final JWFNumberField flameFPSField;

  public AnimationController(TinaController pTinaController, ErrorHandler pErrorHandler, Prefs pPrefs, JPanel pRootPanel,
      JWFNumberField pKeyframesFrameField, JSlider pKeyframesFrameSlider, JWFNumberField pKeyframesFrameCountField,
      JPanel pFrameSliderPanel, JLabel pKeyframesFrameLbl, JLabel pKeyframesFrameCountLbl, JToggleButton pMotionCurveEditModeButton,
      JPanel pMotionBlurPanel, JButton pMotionCurvePlayPreviewButton, JWFNumberField pFlameFPSField) {
    tinaController = pTinaController;
    errorHandler = pErrorHandler;
    prefs = pPrefs;
    rootPanel = pRootPanel;
    keyframesFrameField = pKeyframesFrameField;
    keyframesFrameSlider = pKeyframesFrameSlider;
    keyframesFrameCountField = pKeyframesFrameCountField;
    frameSliderPanel = pFrameSliderPanel;
    keyframesFrameLbl = pKeyframesFrameLbl;
    keyframesFrameCountLbl = pKeyframesFrameCountLbl;
    motionCurveEditModeButton = pMotionCurveEditModeButton;
    motionBlurPanel = pMotionBlurPanel;
    motionCurvePlayPreviewButton = pMotionCurvePlayPreviewButton;
    flameFPSField = pFlameFPSField;
    enableControls();
  }

  public int getCurrFrame() {
    Integer value = keyframesFrameField.getIntValue();
    return (value != null ? value.intValue() : -1);
  }

  public void enableControls() {
    boolean enabled = motionCurveEditModeButton.isSelected();
    for (MotionCurveEditor component : motionPropertyControls) {
      component.setWithMotionCurve(enabled);
    }
    frameSliderPanel.setPreferredSize(new Dimension(0, (enabled ? 28 : 4)));
    keyframesFrameField.setVisible(enabled);
    keyframesFrameSlider.setVisible(enabled);
    keyframesFrameCountField.setVisible(enabled);
    keyframesFrameLbl.setVisible(enabled);
    keyframesFrameCountLbl.setVisible(enabled);
    motionCurvePlayPreviewButton.setVisible(enabled);
  }

  private void adjustFrameControls(int frame) {
    if (keyframesFrameField.getMinValue() >= frame) {
      keyframesFrameField.setMinValue(frame);
    }
    if (keyframesFrameField.getMaxValue() <= frame) {
      keyframesFrameField.setMaxValue(frame);
    }
    keyframesFrameField.setValue(frame);

    if (keyframesFrameCountField.getMaxValue() <= frame) {
      keyframesFrameCountField.setMaxValue(frame);
    }
    if (keyframesFrameCountField.getIntValue() == null || keyframesFrameCountField.getIntValue() <= frame) {
      keyframesFrameCountField.setValue(frame);
    }

    if (keyframesFrameSlider.getMinimum() > frame) {
      keyframesFrameSlider.setMinimum(frame);
    }
    if (keyframesFrameSlider.getMaximum() < frame) {
      keyframesFrameSlider.setMaximum(frame);
    }
    keyframesFrameSlider.setValue(frame);
  }

  public void keyFrameFieldChanged() {
    if (!tinaController.isNoRefresh()) {
      boolean oldNoRefresh = tinaController.isNoRefresh();
      try {
        tinaController.setNoRefresh(true);
        int frame = keyframesFrameField.getIntValue() != null ? keyframesFrameField.getIntValue().intValue() : -1;
        adjustFrameControls(frame);
        if (tinaController.getCurrFlame() != null) {
          tinaController.getCurrFlame().setFrame(frame);
        }
        //// tinaController.refreshFlameImage(true, false, 1, true, false);
      }
      finally {
        tinaController.setNoRefresh(oldNoRefresh);
      }

      tinaController.refreshUI();
    }
  }

  public void keyFrameSliderChanged() {
    if (!tinaController.isNoRefresh()) {
      boolean oldNoRefresh = tinaController.isNoRefresh();
      try {
        tinaController.setNoRefresh(true);
        int frame = keyframesFrameSlider.getValue();
        if (playPreviewThread == null) {
          adjustFrameControls(frame);
        }
        if (tinaController.getCurrFlame() != null) {
          tinaController.getCurrFlame().setFrame(frame);
        }
/*
        if (playPreviewThread != null) {
          FlamePanelConfig cfg = tinaController.getFlamePanelConfig();
          boolean oldNoControls = cfg.isNoControls();
          try {
            cfg.setNoControls(true);
            tinaController.refreshFlameImage(true, true, 1, true, false);
          }
          finally {
            cfg.setNoControls(oldNoControls);
          }
        }
 */
      }
      finally {
        tinaController.setNoRefresh(oldNoRefresh);
      }

      if (playPreviewThread == null) {
        tinaController.refreshUI();
      }
    }
  }

  public void refreshUI() {
    keyframesFrameCountField.setValue(tinaController.getCurrFlame().getFrameCount());
    adjustFrameControls(tinaController.getCurrFlame().getFrame());
  }

  public void registerMotionPropertyControls(MotionCurveEditor pComponent) {
    if (!motionPropertyControls.contains(pComponent)) {
      motionPropertyControls.add(pComponent);
    }
  }

  public void toggleMotionCurveEditing() {
    enableControls();
  }

  public void keyFrameCountFieldChanged() {
    if (!tinaController.isNoRefresh()) {
      boolean oldNoRefresh = tinaController.isNoRefresh();
      try {
        tinaController.setNoRefresh(true);
        int frameCount = keyframesFrameCountField.getIntValue();
        if (tinaController.getCurrFlame() != null) {
          tinaController.getCurrFlame().setFrameCount(frameCount);
        }
      }
      finally {
        tinaController.setNoRefresh(oldNoRefresh);
      }
    }
  }

  public class PlayPreviewThread implements Runnable {
    private boolean finished;
    private boolean forceAbort;
    private final RenderMainFlameThreadFinishEvent finishEvent;

    public PlayPreviewThread(RenderMainFlameThreadFinishEvent pFinishEvent) {
      finishEvent = pFinishEvent;
    }

    @Override
    public void run() {
      finished = forceAbort = false;
      try {
        tinaController.cancelBackgroundRender();
        FlamePanel flamePanel = tinaController.getFlamePreviewHelper().getFlamePanelProvider().getFlamePanel();

        SimpleImage origImage = tinaController.getFlamePreviewHelper().getImage();
        SimpleImage bgImage = origImage.clone();
        boolean oldNoControls = flamePanel.getConfig().isNoControls();

        double quality = Math.min( Math.max(0.1, prefs.getTinaRenderAnimPreviewQuality()), 5.0);
        double previewSize = Math.min( Math.max(0.1, prefs.getTinaRenderAnimPreviewSize()), 1.0);

        try {
          flamePanel.getConfig().setNoControls(true);

          int frameCount = keyframesFrameCountField.getIntValue();
          int frameStart = 1;
          /*
          int frameStart = Math.max(1, keyframesFrameField.getIntValue());
          if(frameStart==frameCount) {
            frameStart = 1;
          }
          */
          long t0 = System.currentTimeMillis();
          for (int i = frameStart; i <= frameCount; i++) {
            if (forceAbort) {
              break;
            }
            long f0 = System.currentTimeMillis();
            keyframesFrameSlider.setValue(i);

            SimpleImage img = tinaController.getFlamePreviewHelper().renderAnimFrame(previewSize, quality);
            ComposeTransformer cT=new ComposeTransformer();
            cT.setForegroundImage(img);
            cT.transformImage(bgImage);
            flamePanel.setImage(bgImage);
            flamePanel.repaint();
            while (true) {
              long f1 = System.currentTimeMillis();
              if (f1 - f0 > 25) {
                break;
              }
              Thread.sleep(1);
            }
          }
          long t1 = System.currentTimeMillis();
          finished = true;
          keyframesFrameField.setValue(keyframesFrameSlider.getValue());
          finishEvent.succeeded((t1 - t0) * 0.001);
        }
        finally {
          flamePanel.getConfig().setNoControls(oldNoControls);
          flamePanel.setImage(origImage);
          // no repaint here to avoid flickering
          // flamePanel.repaint();
        }
      }
      catch (Throwable ex) {
        finished = true;
        keyframesFrameField.setValue(keyframesFrameSlider.getValue());
        finishEvent.failed(ex);
      }
    }

    public boolean isFinished() {
      return finished;
    }

    public void setForceAbort() {
      forceAbort = true;
    }

  }

  private PlayPreviewThread playPreviewThread = null;

  private void enablePlayPreviewControls() {
    motionCurvePlayPreviewButton.setText(playPreviewThread == null ? "Play" : "Stop");
    String iconname = playPreviewThread == null ? "media-playback-start-7" : "media-playback-stop-7";
    motionCurvePlayPreviewButton.setIcon(new ImageIcon(MainEditorFrame.class.getResource("/org/jwildfire/swing/icons/new/"+iconname+".png")));
  }

  public void playPreviewButtonClicked() {
    if (playPreviewThread != null) {
      playPreviewThread.setForceAbort();
      while (playPreviewThread.isFinished()) {
        try {
          Thread.sleep(10);
        }
        catch (InterruptedException e) {
          e.printStackTrace();
        }
      }
      playPreviewThread = null;
      enablePlayPreviewControls();
    }
    else {
      RenderMainFlameThreadFinishEvent finishEvent = new RenderMainFlameThreadFinishEvent() {

        @Override
        public void succeeded(double pElapsedTime) {
          try {
          }
          catch (Throwable ex) {
            errorHandler.handleError(ex);
          }
          playPreviewThread = null;
          enablePlayPreviewControls();
          tinaController.refreshFlameImage(true, false, 1, true, false);
        }

        @Override
        public void failed(Throwable exception) {
          errorHandler.handleError(exception);
          playPreviewThread = null;
          enablePlayPreviewControls();
          tinaController.refreshFlameImage(true, false, 1, true, false);
        }
      };

      playPreviewThread = new PlayPreviewThread(finishEvent);
      enablePlayPreviewControls();
      new Thread(playPreviewThread).start();
    }
  }

  public void skipToLastFrameButtonClicked() {
    if(playPreviewThread == null) {
      keyframesFrameField.setValue(keyframesFrameCountField.getIntValue());
      keyframesFrameSlider.setValue(keyframesFrameField.getIntValue());
    }
  }

  public void skipToFirstFrameButtonClicked() {
    if(playPreviewThread == null) {
      keyframesFrameField.setValue(1);
      keyframesFrameSlider.setValue(keyframesFrameField.getIntValue());
    }
  }
}
