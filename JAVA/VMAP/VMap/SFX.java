package vmap;
import java.*;
import java.awt.*;
import java.awt.image.*;
import java.applet.*;
import java.lang.Math;

public class SFX extends DoubleBufferApplet
{

	int w=0;
	int h=0;

	public SFX() {}

	public int check( int val, int min, int max ) {

		if( val > max )
			val = max;
		else if( val < min )
			val = min;

		return( val );
	}


	public int s2i( String s ) {

		int		value;

		try {
			value = Integer.valueOf( s ).intValue();
		}
		catch( NumberFormatException e ) {
			value = 0;
		}

		return( value );
	}


	public Image makeImage( int[] array ) {

		int		w = array[ 0 ];
		int		h = array[ 1 ];

		return( createImage( new MemoryImageSource( w, h, array, 2, w ) ) );
	}

	public int returnWidth() {

		return( w );
	}

	public int returnHeight() {
		return( h );
	}
	public int[] returnArray( Image img ) {

		h = img.getHeight( this );
		w = img.getWidth( this );
		int[]			array = new int[ w * h + 2 ];
		PixelGrabber  	pg = new PixelGrabber( img, 0, 0, w, h,
		    array, 2, w);

		array[ 0 ] = w;
		array[ 1 ] = h;

		try {
			pg.grabPixels();
		} 
		catch  ( InterruptedException  e ) {
			System.err.println( "Interrupt error in SFX." );
		}
		if  ( (pg.status() & ImageObserver.ABORT) != 0) {
			System.err.println( "Error fetching image in SFX." );
		}

		return( array );
	}

	public int[] contrast( int[] anArray, int cont ) {
		w = anArray[ 0 ];
		h = anArray[ 1 ];

		int             array[] = new int[ w * h + 2 ];
		float			map[] = new float[ 256 ];

		array[ 0 ] = w;
		array[ 1 ] = h;

		// Use the function y=( 1 - ( 1 - x ) ^ cont ) ^ ( 1 / cont )

		    map = generateMap( -1 * cont );

		if( cont != 0 ) {
			for( int i = 0; i < ( h * w ); i++ ) {

				int 	col = anArray[ i + 2 ];
				int		red = ( col & 0xff0000 ) >> 16;
				int		green = ( col & 0xff00 ) >> 8;
				int		blue = ( col & 0xff );

				red = (int)map[ red ];
				green = (int)map[ green ];
				blue = (int)map[ blue ];

				array[ 2 + i ] = ( 255 << 24 | red << 16 | green << 8 ) |
				    blue;
			}

			return( array );
		}
		else
			return( anArray );
	}


	private float[] generateMap( int cont ) {

		float			map[] = new float[ 256 ];
		float			n = (101f-(float)cont)/100f; 

		    for( int i=0; i < 256; i++ ) {

			float			x = (float)i / 256;

			map[ i ] = 256f * (float)Math.pow( (1f - Math.pow( 1f-x, n )), 1f/n);
		}

		return( map );
	}


	public int[] bright( int[] anArray, int a ) {

		int				w = anArray[ 0 ];
		int				h = anArray[ 1 ];
		int             array[] = new int[ w * h + 2 ];

		array[ 0 ] = w;
		array[ 1 ] = h;

		if( a > 0 ) {

			for( int i = 0; i < ( h * w ); i++ ) {

				int 	col = anArray[ i + 2 ];
				int		red = ( col & 0xff0000 ) >> 16;
				int		green = ( col & 0xff00 ) >> 8;
				int		blue = ( col & 0xff );

				red += (int)( (float)( 255 - red ) / 100f * (float)a );
				green += (int)( (float)( 255 - green ) / 100f * (float)a );
				blue += (int)( (float)( 255 - blue ) / 100f * (float)a );

				array[ 2 + i ] = ( 255 << 24 | red << 16 | green << 8 ) |
				    blue;
			}

			return( array );
		}
		else if( a < 0 ) {
			for( int i = 0; i < ( h * w ); i++ ) {

				int     col = anArray[ i + 2 ];
				int     red = ( col & 0xff0000 ) >> 16;
				int     green = ( col & 0xff00 ) >> 8;
				int     blue = ( col & 0xff );

				red -= (int)( (float)red / 100f * -(float)a );
				green -= (int)( (float)green / 100f * -(float)a );
				blue -= (int)( (float)blue / 100f * -(float)a );

				array[ 2 + i ] = ( 255 << 24 | red << 16 | green << 8 |
				    blue );
			}

			return( array );
		}

		return( anArray );
	}

public Image Lightning(Image flash, int light)
	    {
		if (light > 75) {light=75;}
		if (light < 1)  {light=1;}
		return makeImage(bright(returnArray(flash),light));
	}

public int LightningRod(int light, Image flash)
	{
		int cached=0;
		h = flash.getHeight( this );
		w = flash.getWidth( this );
		int array[] = new int[ w * h + 2 ];
		Image newImage=null;
		if (cached==0){
		try {array=returnArray(flash);} catch(Exception e) { cached=1; } }
		if (cached==0){
		try {array=bright(array,light);} catch(Exception e) { cached=2; } }
		if (cached==0){
		try {newImage=makeImage(array);} catch(Exception e) { cached=3; } }
		return cached;
	}

public static Image Stretch(int squishx, int squishy, Image squash)
	    {
		// Returns a scaled/stretched image.
		    squash=squash.getScaledInstance(squishx,squishy,Image.SCALE_DEFAULT);
		    return squash;
	}

public int[] negate( int[] anArray) {

                int w = anArray[ 0 ];
                int h = anArray[ 1 ];
                int array[] = new int[ w * h + 2 ];
                                
                array[ 0 ] = w;
                array[ 1 ] = h;
                                
                        for( int i = 0; i < ( h * w ); i++ ) {
                                
                                int col = anArray[ i + 2 ];
                                int red = ( col & 0xff0000 ) >> 16;
                                int green = ( col & 0xff00 ) >> 8;
                                int blue = ( col & 0xff );
                        
                                red += (int)( (float)( 255 - red ));
                                green += (int)( (float)( 255 - green ));
                                blue += (int)( (float)( 255 - blue ));
                
                                array[ 2 + i ] = ( 255 << 24 | red << 16 | green << 8 ) |
                                    blue;
                        }
                        
                        return( array );
        }
                
public Image Invert(Image flash)
            {
                return makeImage(negate(returnArray(flash)));
        }
}
