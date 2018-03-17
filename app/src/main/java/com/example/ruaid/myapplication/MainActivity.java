package com.example.ruaid.myapplication;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.ShareActionProvider;
import android.widget.TextView;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Queue;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener{

    public static final int MY_PERMISSIONS_REQUEST_CAMERA = 100;
    public static final String ALLOW_KEY = "ALLOWED";
    public static final String CAMERA_PREF = "camera_pref";
    Bitmap picture;
    Bitmap floodFilled;
    public static Boolean lock = false,
    iconSelect = false;   // check if lock image is pressed so image can be changed from touch image to image view
    int tolerance = 7;
    int startX, startY;
    int targetColour = 0;
    int grey = 0;
    String shade = "";
    String circle = "O";
    int count =50; // single shade selector for touching bottom of screen
    TextView shadeText;
    TextView shadeCircle;
    public static final int PICK_IMAGE = 2; // request code for onActivity result

    SeekBar brightnessBar;
    TextView brightnessText;
    Button doProcess;
    SeekBar toleranceBar;
    TextView toleranceText;
    Button accept;
    int brightnessValue = 0;
    Drawable floodIcon;
    Drawable lockIcon;
    Uri imageUri;


    public boolean bfab = false;

    Context context;
    DashedLineView dv ;     // getting the dashed lines
    public static boolean touched;

    public static Queue<Point> q;

    Canvas canvas;


    private ShareActionProvider mShareActionProvider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.start);
        context = this;
        // 2 Options on the start menu

        ImageView camera = (ImageView) findViewById(R.id.camera_select);    // if user opens camera, intent 101 will run
        buttonEffect(camera);
        camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openCamera();
            }
        });

        ImageView gallery = (ImageView) findViewById(R.id.gallery_select);  // if iser selects image, intent 2 will run
        buttonEffect(gallery);
        gallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pickImage();
            }
        });
    }

    //animation of the two images in the main menu
    public static void buttonEffect(View button) {

        button.setOnTouchListener(new View.OnTouchListener() {

            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN: {
                        v.getBackground().setColorFilter(0xe0f47521, PorterDuff.Mode.SRC_ATOP);
                        v.invalidate();
                        break;
                    }
                    case MotionEvent.ACTION_UP: {
                        v.getBackground().clearColorFilter();
                        int mCurrRotation = 0;
                        mCurrRotation %= 360;
                        float fromRotation = mCurrRotation;
                        float toRotation = mCurrRotation += 360;

                        final RotateAnimation rotateAnim = new RotateAnimation(
                                fromRotation, toRotation, v.getWidth() / 2, v.getHeight() / 2);

                        rotateAnim.setDuration(500);
                        rotateAnim.setFillAfter(true); // Must be true or the animation will reset

                        final View viewToAnimate = v;
                        v.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                viewToAnimate.setVisibility(View.GONE);
                            }
                        }, 1000);
                        v.startAnimation(rotateAnim);
                        v.invalidate();
                        break;
                    }
                }
                return false;
            }
        });
    }


    private void openCamera() {
        Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
        File filePhoto = new File(Environment.getExternalStorageDirectory(), "Pic.jpg");
        imageUri = Uri.fromFile(filePhoto);
        startActivityForResult(intent, 101);         // start the camera app within the phone.
    }


    public void pickImage() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE);
    }

    // OpenCamera() and pickImage() both lead to onActivityResult, with different request codes, either 101 or 2
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {     // the result of the camera intent, the image being captured and approved leads here
        if (resultCode != RESULT_CANCELED) {
            if (requestCode == 101) {
                picture = (Bitmap) data.getExtras().get("data");
                picture = toGrayscale(picture);
                setView();
            }
        }
        if (requestCode == 2) {
            super.onActivityResult(requestCode, resultCode, data);
            if (data == null) {
                //Display an error
                return;
            }
            Uri selectedImage = data.getData();
            InputStream image_stream = null;
            try {
                image_stream = getContentResolver().openInputStream(selectedImage);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            picture = BitmapFactory.decodeStream(image_stream);
            picture = toGrayscale(picture);
            floodFilled = picture;
            setShareIntent(data);
            setView();
        }
    }


    private void setView(){
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "The Selected Shade is: " + shade, Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                if(bfab){
                    shadeText = (TextView) findViewById(R.id.shadeText);
                    shadeText.setVisibility(View.INVISIBLE);
                    bfab=false;
                }
                else{
                    shadeText = (TextView) findViewById(R.id.shadeText);
                    shadeText.setVisibility(View.VISIBLE);
                    bfab=true;
                }

            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        imageChosen();
    }

    //  After the activity result the image is presented. This uses the content main xml file
    private void imageChosen() {
        final TouchImageView viewBitmap = (TouchImageView) findViewById(R.id.bitmapView);
        viewBitmap.setImageBitmap(picture);

        shadeText = (TextView) findViewById(R.id.shadeText);
        shadeText.setVisibility(View.INVISIBLE);
        shadeCircle = (TextView) findViewById(R.id.shadeCircle);
        shadeCircle.setVisibility(View.INVISIBLE);

        viewBitmap.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        startX = (int) event.getX();       // include try and catch to prevent crash
                        startY = (int) event.getY();

                        Matrix inverse = new Matrix();      // translates x and y coords of touch onto bitmap values
                        viewBitmap.getImageMatrix().invert(inverse);
                        float[] touchPoint = new float[]{event.getX(), event.getY()};
                        inverse.mapPoints(touchPoint);
                        int xCoord = (int) touchPoint[0];
                        int yCoord = (int) touchPoint[1];

                        // if the user selects outside the image -1 is returned and a toast shows
                        targetColour = getColour(xCoord, yCoord);
                        grey = (int) (targetColour & 0xFF);  // bitmask the pixel data to defeat sign extension in the int
                        ShadeSelector s = new ShadeSelector();
                        shade = s.shade(grey);

                        if (targetColour == -1) {
                            Snackbar.make(v, "Please Select Inside the Image ", Snackbar.LENGTH_LONG)
                                    .setAction("Action", null).show();
                        } else {
                            Snackbar.make(v, "The Selected Shade is: " + shade, Snackbar.LENGTH_LONG)
                                    .setAction("Action", null).show();
                        }

                        Log.d("targetColour", String.valueOf(targetColour));
                        int replacementColour = 0xffffffff;

                        Point pt = new Point();
                        pt.x = xCoord;
                        pt.y = yCoord;
                        FloodFill f = new FloodFill();
                        try {    // this catches above and below the image that occurs when the image is chosen and not taken.
                            q = f.floodFill_array(picture, pt, targetColour, replacementColour, tolerance);

                            shadeText = (TextView) findViewById(R.id.shadeText);
                            //shadeText.setVisibility(View.VISIBLE);
                            //shadeText.setTextColor();
                            shadeText.setText(shade);
                            shadeText.animate()
                                    // ensure the text doesnt go off screen
                                    // changes tex to match shade

                                    .x(event.getRawX()-50)
                                    .y(event.getRawY()-200)
                                    .setDuration(0)
                                    .start();

                            touched = true;     // for drawing on canvas
                            //dv = new DashedLineView(context);

                        } catch (ArrayIndexOutOfBoundsException e) {
                            e.printStackTrace();
                            Snackbar.make(v, "Please Select Inside the Image ", Snackbar.LENGTH_LONG)
                                    .setAction("Action", null).show();
                        }
                    }
                return true;
            }
        });
    }

    //  NavigationItem Calls this to stop the flood fil on touch and start the single shade
    public void singleShadeSelect(){

        final TouchImageView viewBitmap = (TouchImageView) findViewById(R.id.bitmapView);
        viewBitmap.setImageBitmap(picture);

        viewBitmap.setOnTouchListener(new View.OnTouchListener() {
            float dX, dY;
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                switch (event.getAction()) {

                    case MotionEvent.ACTION_DOWN:
                        break;

                    case MotionEvent.ACTION_MOVE:
                        shadeText = (TextView) findViewById(R.id.shadeText);
                        shadeText.setVisibility(View.VISIBLE);
                        shadeCircle = (TextView) findViewById(R.id.shadeCircle);
                        shadeCircle.setVisibility(View.VISIBLE);
                        startX = (int) event.getX();       // include try and catch to prevent crash
                        startY = (int) event.getY();

                        Matrix inverse = new Matrix();      // translates x and y coords of touch onto bitmap values
                        viewBitmap.getImageMatrix().invert(inverse);
                        float[] touchPoint = new float[]{event.getX(), event.getY()};
                        inverse.mapPoints(touchPoint);
                        int xCoord = (int) touchPoint[0];
                        int yCoord = (int) touchPoint[1];

                        // if the user selects outside the image -1 is returned and a toast shows
                        targetColour = getColour(xCoord, yCoord);
                        grey = (int) (targetColour & 0xFF);  // bitmask the pixel data to defeat sign extension in the int
                        ShadeSelector s = new ShadeSelector();
                        shade = s.shade(grey);
                        shadeText.setText(shade);
                        shadeText.setTextColor(targetColour);
                        shadeCircle.setText(circle);

                        if(event.getRawY()>1460){
                            count=450;
                        }
                        else
                            count=0;
                        shadeCircle.animate()
                                // ensure the text doesnt go off screen
                                // changes tex to match shade
                                .x(event.getRawX()-50)
                                .y(event.getRawY()-300)
                                .setDuration(0)
                                .start();

                        shadeText.animate()
                                // ensure the text doesnt go off screen
                                // changes tex to match shade
                                .x(event.getRawX()-50)
                                .y(event.getRawY()-count)
                                .setDuration(0)
                                .start();
                        break;
                    default:
                        return false;
                }
                return true;
            }
        });
    }


    public int getColour(int x, int y) {
        try {
            int pixelValue = picture.getPixel(x, y);     // grey will have the same r g and b value, so only need to check for one***
            return pixelValue;
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
        return -1;      // return -1 if user touches outside the image
    }


    public Bitmap toGrayscale(Bitmap bitmap) {
        int width, height;
        height = bitmap.getHeight();    // get height for mapped bitmap
        width = bitmap.getWidth();

        Bitmap bmpGrayscale = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);  // Create new Bitmap to map original Bitmap
        Canvas c = new Canvas(bmpGrayscale);                                                // onto with saturation set to 0
        Paint paint = new Paint();
        ColorMatrix cm = new ColorMatrix();
        cm.setSaturation(0);
        ColorMatrixColorFilter f = new ColorMatrixColorFilter(cm);
        paint.setColorFilter(f);        // set colour with saturation set to 0.
        c.drawBitmap(bitmap, 0, 0, paint);      // draw bitmap on canvas
        return bmpGrayscale;
    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            new AlertDialog.Builder(this)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setTitle("Exit Pencil Down")
                    .setMessage("Are you sure you want to leave?")
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    })
                    .setNegativeButton("No", null)
                    .show();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        // Locate MenuItem with ShareActionProvider
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            setTolerance();
            return true;
        }

        if (id == R.id.action_lock) {       // lock is accessed from touch image view class
            if (lock) {
                lockIcon = getResources().getDrawable(R.drawable.lock); // The ID of your drawable.
                item.setIcon(lockIcon);
                lock = false;
            } else {
                lockIcon = getResources().getDrawable(R.drawable.unlock); // The ID of your drawable.
                item.setIcon(lockIcon);
                lock = true;
            }
            return true;
        }

        if (id == R.id.action_single) {
            if(!iconSelect) {
                singleShadeSelect();
                floodIcon = getResources().getDrawable(R.drawable.group); // The ID of your drawable.
                item.setIcon(floodIcon);
                iconSelect = true;
            }
            else{
                imageChosen();
                floodIcon = getResources().getDrawable(R.drawable.single); // The ID of your drawable.
                item.setIcon(floodIcon);
                iconSelect=false;
            }

            return true;
        }


        return super.onOptionsItemSelected(item);
    }


    public void setTolerance() {
        brightnessBar = (SeekBar) findViewById(R.id.brightnessBar);
        brightnessText = (TextView) findViewById(R.id.brightnessText);
        doProcess = (Button) findViewById(R.id.doProcess);
        brightnessBar.setVisibility(View.INVISIBLE);
        brightnessText.setVisibility(View.INVISIBLE);
        doProcess.setVisibility(View.INVISIBLE);


        toleranceBar = (SeekBar) findViewById(R.id.toleranceBar);
        toleranceText = (TextView) findViewById(R.id.toleranceText);
        accept = (Button) findViewById(R.id.accept);
        toleranceBar.setVisibility(View.VISIBLE);
        toleranceText.setVisibility(View.VISIBLE);
        accept.setVisibility(View.VISIBLE);

        View.OnClickListener doProcessClickListener
                = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                toleranceBar.setVisibility(View.INVISIBLE);
                toleranceText.setVisibility(View.INVISIBLE);
                accept.setVisibility(View.INVISIBLE);
            }
        };
        accept.setOnClickListener(doProcessClickListener);
        toleranceBar.setMax(20);
        SeekBar.OnSeekBarChangeListener brightnessBarChangeListener
                = new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress,
                                          boolean fromUser) {
                tolerance = progress;
                toleranceText.setText(String.valueOf(tolerance));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }
        };
        toleranceBar.setOnSeekBarChangeListener(brightnessBarChangeListener);


    }


    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            openCamera();


        } else if (id == R.id.nav_gallery) {
            setBrightness();

        } else if (id == R.id.nav_slideshow) {
            Sketch sketch = new Sketch();
            picture = sketch.changeToSketch(picture);

            final TouchImageView viewBitmap = (TouchImageView) findViewById(R.id.bitmapView);
            viewBitmap.setImageBitmap(picture);


        } else if (id == R.id.nav_manage) {
            pickImage();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    // Call to update the share intent
    private void setShareIntent(Intent shareIntent) {
        if (mShareActionProvider != null) {
            mShareActionProvider.setShareIntent(shareIntent);
        }
    }


    public void setBrightness() {
        toleranceBar = (SeekBar) findViewById(R.id.toleranceBar);
        toleranceText = (TextView) findViewById(R.id.toleranceText);
        accept = (Button) findViewById(R.id.accept);
        toleranceBar.setVisibility(View.INVISIBLE);
        toleranceText.setVisibility(View.INVISIBLE);
        accept.setVisibility(View.INVISIBLE);


        brightnessBar = (SeekBar) findViewById(R.id.brightnessBar);
        brightnessText = (TextView) findViewById(R.id.brightnessText);
        doProcess = (Button) findViewById(R.id.doProcess);
        brightnessBar.setVisibility(View.VISIBLE);
        brightnessText.setVisibility(View.VISIBLE);


        View.OnClickListener doProcessClickListener
                = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub

            }
        };
        doProcess.setOnClickListener(doProcessClickListener);
        SeekBar.OnSeekBarChangeListener brightnessBarChangeListener
                = new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress,
                                          boolean fromUser) {
                brightnessValue = progress ;
                brightnessText.setText(String.valueOf(brightnessValue));
                Brightness bright = new Brightness();
                Bitmap b = bright.brightness(picture, brightnessValue);
                final TouchImageView viewBitmap = (TouchImageView) findViewById(R.id.bitmapView);
                viewBitmap.setImageBitmap(b);
            }


            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }
        };
        brightnessBar.setOnSeekBarChangeListener(brightnessBarChangeListener);
    }


    public static void saveToPreferences(Context context, String key, Boolean allowed) {
        SharedPreferences myPrefs = context.getSharedPreferences(CAMERA_PREF,
                Context.MODE_PRIVATE);
        SharedPreferences.Editor prefsEditor = myPrefs.edit();
        prefsEditor.putBoolean(key, allowed);
        prefsEditor.commit();
    }


    @NonNull
    public static Boolean getFromPref(Context context, String key) {
        SharedPreferences myPrefs = context.getSharedPreferences(CAMERA_PREF,
                Context.MODE_PRIVATE);
        return (myPrefs.getBoolean(key, false));
    }


    private void showAlert() {
        AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
        alertDialog.setTitle("Alert");
        alertDialog.setMessage("App needs to access the camera.");

        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "DONT ALLOW",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        finish();
                    }
                });

        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "ALLOW",
                new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        ActivityCompat.requestPermissions(MainActivity.this,
                                new String[]{Manifest.permission.CAMERA},
                                MY_PERMISSIONS_REQUEST_CAMERA);
                    }
                });
        alertDialog.show();
    }


    private void showSettingsAlert() {
        AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
        alertDialog.setTitle("Alert");
        alertDialog.setMessage("App needs to access the camera.");

        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "DONT ALLOW",
                new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        //finish();
                    }
                });

        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "SETTINGS",
                new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        startInstalledAppDetailsActivity(MainActivity.this);
                    }
                });

        alertDialog.show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_CAMERA: {
                for (int i = 0, len = permissions.length; i < len; i++) {
                    String permission = permissions[i];

                    if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                        boolean
                                showRationale =
                                ActivityCompat.shouldShowRequestPermissionRationale(
                                        this, permission);

                        if (showRationale) {
                            showAlert();
                        } else if (!showRationale) {
                            // user denied flagging NEVER ASK AGAIN
                            // you can either enable some fall back,
                            // disable features of your app
                            // or open another dialog explaining
                            // again the permission and directing to
                            // the app setting
                            saveToPreferences(MainActivity.this, ALLOW_KEY, true);
                        }
                    }
                }
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
    }


    public static void startInstalledAppDetailsActivity(final Activity context) {
        if (context == null) {
            return;
        }



        final Intent i = new Intent();
        i.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        i.addCategory(Intent.CATEGORY_DEFAULT);
        i.setData(Uri.parse("package:" + context.getPackageName()));
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        i.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        i.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        context.startActivity(i);
    }


}