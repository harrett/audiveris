//------------------------------------------------------------------------------------------------//
//                                                                                                //
//                                A c c i d N o t e R e l a t i o n                               //
//                                                                                                //
//------------------------------------------------------------------------------------------------//
// <editor-fold defaultstate="collapsed" desc="hdr">
//  Copyright © Herve Bitteur and others 2000-2014. All rights reserved.
//  This software is released under the GNU General Public License.
//  Goto http://kenai.com/projects/audiveris to report bugs or suggestions.
//------------------------------------------------------------------------------------------------//
// </editor-fold>
package omr.sig;

import omr.constant.Constant;
import omr.constant.ConstantSet;

import omr.sheet.Scale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class {@code AccidNoteRelation} represents the relation support between an accidental
 * alteration (sharp, flat, natural, double-sharp, double-flat) and a note.
 *
 * @author Hervé Bitteur
 */
public class AccidNoteRelation
        extends AbstractConnection
{
    //~ Static fields/initializers -----------------------------------------------------------------

    private static final Constants constants = new Constants();

    private static final Logger logger = LoggerFactory.getLogger(AccidNoteRelation.class);

    private static final double[] IN_WEIGHTS = new double[]{
        constants.xInWeight.getValue(),
        constants.yWeight.getValue()
    };

    private static final double[] OUT_WEIGHTS = new double[]{
        constants.xOutWeight.getValue(),
        constants.yWeight.getValue()
    };

    //~ Constructors -------------------------------------------------------------------------------
    /**
     * Creates a new AccidNoteRelation object.
     */
    public AccidNoteRelation ()
    {
    }

    //~ Methods ------------------------------------------------------------------------------------
    //---------//
    // getName //
    //---------//
    @Override
    public String getName ()
    {
        return "Accid-Note";
    }

    //------------------//
    // getXInGapMaximum //
    //------------------//
    public static Scale.Fraction getXInGapMaximum ()
    {
        return constants.xInGapMax;
    }

    //-------------------//
    // getXOutGapMaximum //
    //-------------------//
    public static Scale.Fraction getXOutGapMaximum ()
    {
        return constants.xOutGapMax;
    }

    //----------------//
    // getYGapMaximum //
    //----------------//
    public static Scale.Fraction getYGapMaximum ()
    {
        return constants.yGapMax;
    }

    //--------------//
    // getInWeights //
    //--------------//
    @Override
    protected double[] getInWeights ()
    {
        return IN_WEIGHTS;
    }

    //---------------//
    // getOutWeights //
    //---------------//
    @Override
    protected double[] getOutWeights ()
    {
        return OUT_WEIGHTS;
    }

    //----------------//
    // getTargetCoeff //
    //----------------//
    /**
     * AccidNoteRelation brings no support on target (Note) side.
     *
     * @return 0
     */
    @Override
    protected double getTargetCoeff ()
    {
        return 0.0;
    }

    @Override
    protected Scale.Fraction getXInGapMax ()
    {
        return getXInGapMaximum();
    }

    @Override
    protected Scale.Fraction getXOutGapMax ()
    {
        return getXOutGapMaximum();
    }

    @Override
    protected Scale.Fraction getYGapMax ()
    {
        return getYGapMaximum();
    }

    //~ Inner Classes ------------------------------------------------------------------------------
    //-----------//
    // Constants //
    //-----------//
    private static final class Constants
            extends ConstantSet
    {
        //~ Instance fields ------------------------------------------------------------------------

        final Scale.Fraction yGapMax = new Scale.Fraction(
                0.4,
                "Maximum vertical gap between accid & note");

        final Scale.Fraction xInGapMax = new Scale.Fraction(
                0.2,
                "Maximum horizontal overlap between accid & note");

        final Scale.Fraction xOutGapMax = new Scale.Fraction(
                2.0,
                "Maximum horizontal gap between accid & note");

        final Constant.Ratio xInWeight = new Constant.Ratio(1, "Relative impact weight for xInGap");

        final Constant.Ratio xOutWeight = new Constant.Ratio(
                1,
                "Relative impact weight for xOutGap");

        final Constant.Ratio yWeight = new Constant.Ratio(4, "Relative impact weight for yGap");
    }
}