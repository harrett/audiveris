//-----------------------------------------------------------------------//
//                                                                       //
//                         Z o o m e d P a n e l                         //
//                                                                       //
//  Copyright (C) Herve Bitteur 2000-2006. All rights reserved.          //
//  This software is released under the terms of the GNU General Public  //
//  License. Please contact the author at herve.bitteur@laposte.net      //
//  to report bugs & suggestions.                                        //
//-----------------------------------------------------------------------//

package omr.ui.view;

import omr.constant.Constant;
import omr.constant.ConstantSet;
import omr.util.Logger;
import omr.selection.Selection;
import omr.selection.SelectionHint;
import omr.selection.SelectionObserver;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;

/**
 * Class <code>ZoomedPanel</code> is a class meant to handle common
 * task of a display with a magnifying lens, as provided by a related
 * {@link Zoom} entity.
 * 
 * <p>This class does not allocate any zoom instance. When using this
 * class, we have to provide our own Zoom instance, either at contruction
 * time by using the proper constructor or later by using the {@link
 * #setZoom} method. The class then registers itself as an observer of the
 * Zoom instance, to be notified when the zoom ratio is modified.
 * 
 * <p>The ModelSize is the unzoomed size of the data to be displayed, it
 * can be updated through {@link #setModelSize}. This is useful when used
 * in combination with a JScrollPane container (see {@link ScrollView}
 * example).
 * 
 * <dl>
 * <dt><b>Selection Inputs:</b></dt><ul>
 * <li>PIXEL Location | SCORE Location
 * </ul>
 * 
 * <dt><b>Selection Outputs:</b></dt><ul>
 * <li>PIXEL Location | SCORE Location (either flagged with LOCATION_INIT hint)
 * </ul>
 * </dl>
 * 
 * 
 * @author Herv&eacute; Bitteur
 * @version $Id$
 */

public class ZoomedPanel
    extends JPanel
    implements ChangeListener,          // To receive events from Zoom
               MouseMonitor,            // To receive mouse events from Rubber
               SelectionObserver        // To receive events for location selection
{
    //~ Static variables/initializers -------------------------------------

    private static final Logger logger = Logger.getLogger(ZoomedPanel.class);
    private static final Constants constants = new Constants();

    //~ Instance variables ------------------------------------------------

    /** Model size (independent of display zoom) */
    protected Dimension modelSize;

    /** Current display zoom */
    protected Zoom zoom;

    /** Related location Selection if any */
    protected Selection locationSelection;

    //~ Constructors ------------------------------------------------------

    //-------------//
    // ZoomedPanel //
    //-------------//
    /**
     * Create a zoomed panel, with no predefined zoom, assuming a zoom
     * instance will be provided later via the {@link #setZoom} method..
     */
    public ZoomedPanel ()
    {
    }

    //-------------//
    // ZoomedPanel //
    //-------------//
    /**
     * Create a zoomed panel, with a driving zoom instance.
     *
     * @param zoom the related zoom instance
     */
    public ZoomedPanel (Zoom zoom)
    {
        setZoom(zoom);
    }

    //~ Methods -----------------------------------------------------------

    //----------------------//
    // setLocationSelection //
    //----------------------//
    /**
     * Allow to inject a dependency on a location Selection object. This
     * location Selection is used for two purposes: <ol>
     *
     * <li>First, this panel is a producer of location information. The
     * location can be modified both programmatically (by calling method
     * {@link #setFocusLocation}) and interactively by mouse event
     * (pointSelected or rectangleSelected which in turn call
     * setFocusLocation) .</li>
     *
     * <li>Second, this panel is a consumer of location information, since
     * it makes the selected location visible in the display, through the
     * method {@link #showFocusLocation}.</li> </ol>
     *
     * <p><b>Nota</b>: Setting the location selection does not
     * automatically register this view on the selection object. If
     * such registering is needed, it must be done manually.
     *
     * @param locationSelection the proper location Selection to be updated
     */
    public void setLocationSelection (Selection locationSelection)
    {
        if (this.locationSelection != null) {
            this.locationSelection.deleteObserver(this);
        }

        this.locationSelection = locationSelection;
    }

    //----------------//
    // getPanelCenter //
    //----------------//
    /**
     * Retrieve the current center of the display, and report its
     * corresponding model location.
     *
     * @return the unscaled coordinates of the panel center
     */
    public Point getPanelCenter ()
    {
        Rectangle vr = getVisibleRect();
        Point pt = new Point(zoom.unscaled(vr.x + (vr.width / 2)),
                             zoom.unscaled(vr.y + (vr.height / 2)));

        if (logger.isFineEnabled()) {
            logger.fine("getPanelCenter=" + pt);
        }

        return pt;
    }

    //---------//
    // getZoom //
    //---------//
    /**
     * Return the current zoom
     *
     * @return the used zoom
     */
    public Zoom getZoom ()
    {
        return zoom;
    }

    //-----------------//
    // contextSelected //
    //-----------------//
    public void contextSelected (MouseEvent e,
                                 Point      pt)
    {
        // Nothing by default
    }

    //---------------//
    // pointSelected //
    //---------------//
    /**
     * Point designation, which does nothing except notifying registered
     * observers about the designated point.
     *
     * @param e the mouse event
     * @param pt the selected point in model pixel coordinates
     */
    public void pointSelected (MouseEvent e,
                               Point      pt)
    {
        setFocusLocation(new Rectangle(pt));
    }

    //------------//
    // pointAdded //
    //------------//
    public void pointAdded (MouseEvent e,
                            Point      pt)
    {
        pointSelected(e, pt);
    }

    //-------------------//
    // rectangleSelected //
    //-------------------//
    public void rectangleSelected (MouseEvent e,
                                   Rectangle  rect)
    {
        setFocusLocation(rect);
    }

    //-----------------//
    // rectangleZoomed //
    //-----------------//
    public void rectangleZoomed (MouseEvent      e,
                                 final Rectangle rect)
    {
        // First focus on center of the specified rectangle
        showFocusLocation(rect);

        // Then, adjust zoom ratio to fit the rectangle size
        SwingUtilities.invokeLater(new Runnable()
            {
                public void run ()
                {
                    Rectangle vr = getVisibleRect();
                    double zoomX = (double) vr.width / (double) rect.width;
                    double zoomY = (double) vr.height / (double) rect.height;
                    zoom.setRatio(Math.min(zoomX, zoomY));
                }
            });
    }

    //------------------//
    // setFocusLocation //
    //------------------//
    /**
     * Modifies the location information. This method simply posts the
     * location information on the proper Selection object, provided that
     * such object has been previously injected (by means of the method
     * {@link #setLocationSelection}.
     *
     * @param rect the location information
     */
    public void setFocusLocation (Rectangle rect)
    {
        if (logger.isFineEnabled()) {
            logger.fine("setFocusRectangle rect=" + rect);
        }

        // Write & forward the new pixel selection
        if (locationSelection != null) { // Producer
            locationSelection.setEntity
                (rect != null ? new Rectangle(rect) : null,
                 SelectionHint.LOCATION_INIT);
        }
    }

    //-------------------//
    // showFocusLocation //
    //-------------------//
    /**
     * Update the display, so that the location rectangle gets visible.
     *
     * <b>NOTA</b>: Subclasses that override this method should call this
     * super implementation or the display will not be updated by default.
     *
     * @param rect the location information
     */
    public void showFocusLocation (Rectangle rect)
    {
        if (logger.isFineEnabled()) {
            logger.fine("showFocusRectangle rect=" + rect);
        }

        updatePreferredSize();

        if (rect != null) {
            // Check whether the rectangle is fully visible,
            // if not, scroll so as to make (most of) it visible
            Rectangle scaledRect = zoom.scaled(rect);
            // Needed to work around a strange behavior of 'contains' method
            if (scaledRect.width == 0) scaledRect.width = 1;
            if (scaledRect.height== 0) scaledRect.height= 1;

            if (!getVisibleRect().contains(scaledRect)) {
                int margin = constants.focusMargin.getValue();
                scrollRectToVisible(new Rectangle
                                    (zoom.scaled(rect.x) - margin,
                                     zoom.scaled(rect.y) - margin,
                                     zoom.scaled(rect.width   + 2 * margin),
                                     zoom.scaled(rect.height) + 2 * margin));
            }
        }

        repaint();
    }

    //---------//
    // setZoom //
    //---------//
    /**
     * Assign a zoom to this panel
     *
     * @param zoom the zomm assigned
     */
    public void setZoom (final Zoom zoom)
    {
        // Clean up if needed
        if (this.zoom != null) {
            this.zoom.removeChangeListener(this);
        }

        this.zoom = zoom;

        if (zoom != null) {
            // Add a listener on this zoom
            zoom.addChangeListener(this);
        }
    }

    //--------------//
    // stateChanged //
    //--------------//
    /**
     * Entry called when the ratio of the related zoom has changed
     *
     * @param e the zoom event
     */
    public void stateChanged (ChangeEvent e)
    {
        // Force a redisplay
        if (locationSelection != null) {
            showFocusLocation((Rectangle) locationSelection.getEntity());
        } else {
            showFocusLocation(null);
        }
    }

    //--------------//
    // getModelSize //
    //--------------//
    /**
     * Report the size of the model object, that is the unscaled size.
     *
     * @return the original size
     */
    protected Dimension getModelSize()
    {
        if (modelSize != null)
            return new Dimension(modelSize);
        else
            return null;
    }

    //--------------//
    // setModelSize //
    //--------------//
    /**
     * Assign the size of the model object, that is the unscaled size.
     */
    public void setModelSize(Dimension modelSize)
    {
        this.modelSize = new Dimension(modelSize);
    }

    //---------------------//
    // updatePreferredSize //
    //---------------------//
    private void updatePreferredSize()
    {
        setPreferredSize(zoom.scaled(getModelSize()));
        revalidate();
    }

    //--------//
    // update //
    //--------//
    /**
     * Call-back (part of Observer interface) triggered when location
     * Selection has been modified, provided that such object has been
     * previously injected (by means of {@link #setLocationSelection}.
     *
     * @param selection the Location Selection
     * @param hint potential notification hint
     */
    public void update (Selection selection,
                        SelectionHint hint)
    {
        ///logger.info("ZoomedPanel. selection=" + selection + " hint=" + hint);

        switch (selection.getTag()) {
        case PIXEL :    // For sheet display
        case SCORE :    // For score display
            Rectangle rect = (Rectangle) locationSelection.getEntity();
            if (rect != null) {
                showFocusLocation(rect);
            }
            break;

        default :
        }
    }

    //~ Classes -----------------------------------------------------------

    //-----------//
    // Constants //
    //-----------//
    private static class Constants
        extends ConstantSet
    {
        Constant.Integer focusMargin = new Constant.Integer
                (20,
                 "Margin (in pixels) visible around a focus");

        Constants ()
        {
            initialize();
        }
    }
}
