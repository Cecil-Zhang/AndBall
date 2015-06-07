package com.nju.andball;

import javax.microedition.khronos.opengles.GL10;

import org.anddev.andengine.engine.Engine;
import org.anddev.andengine.engine.camera.Camera;
import org.anddev.andengine.engine.options.EngineOptions;
import org.anddev.andengine.engine.options.EngineOptions.ScreenOrientation;
import org.anddev.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
import org.anddev.andengine.entity.modifier.ScaleAtModifier;
import org.anddev.andengine.entity.scene.Scene;
import org.anddev.andengine.entity.scene.menu.MenuScene;
import org.anddev.andengine.entity.scene.menu.MenuScene.IOnMenuItemClickListener;
import org.anddev.andengine.entity.scene.menu.item.IMenuItem;
import org.anddev.andengine.entity.scene.menu.item.SpriteMenuItem;
import org.anddev.andengine.entity.scene.menu.item.TextMenuItem;
import org.anddev.andengine.entity.scene.menu.item.decorator.ColorMenuItemDecorator;
import org.anddev.andengine.entity.scene.menu.item.decorator.ScaleMenuItemDecorator;
import org.anddev.andengine.entity.sprite.Sprite;
import org.anddev.andengine.entity.util.FPSLogger;
import org.anddev.andengine.opengl.font.Font;
import org.anddev.andengine.opengl.font.FontFactory;
import org.anddev.andengine.opengl.texture.Texture;
import org.anddev.andengine.opengl.texture.TextureOptions;
import org.anddev.andengine.opengl.texture.region.TextureRegion;
import org.anddev.andengine.opengl.texture.region.TextureRegionFactory;
import org.anddev.andengine.ui.activity.BaseGameActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Handler;
import android.widget.Toast;

public class StartActivity extends BaseGameActivity implements
		IOnMenuItemClickListener {
	// 摄像头尺寸
	private static final int CAMERA_WIDTH = 800;
	private static final int CAMERA_HEIGHT = 480;
	private Camera mCamera;
	protected static final int MENU_PLAY = 0;
	protected static final int MENU_SCORES = MENU_PLAY + 1;
	protected static final int MENU_OPTIONS = MENU_SCORES + 1;
	protected static final int MENU_ABOUT = MENU_OPTIONS + 1;
	protected static final int MENU_EXIT = MENU_ABOUT + 1;
	protected Scene mMenuScene;

	private Texture mMenuBGTexture;
	private TextureRegion mMenuBGTextureRegion;

	private Texture mExitTexture;
	private TextureRegion mExitTextureRegion;

	protected MenuScene mStaticMenuScene;
	private Font mfont;
	protected Texture mFontTexture;
	protected Handler mHandler;

	@Override
	public void onLoadComplete() {
		// TODO Auto-generated method stub

	}

	@Override
	public Engine onLoadEngine() {
		mHandler=new Handler();
		this.mCamera = new Camera(0, 0, CAMERA_WIDTH, CAMERA_HEIGHT);

		// TODO Auto-generated method stub
		return new Engine(new EngineOptions(true, ScreenOrientation.LANDSCAPE,
				new RatioResolutionPolicy(CAMERA_WIDTH, CAMERA_HEIGHT),
				this.mCamera));
	}

	@Override
	public void onLoadResources() {
		// TODO Auto-generated method stub
		this.mFontTexture = new Texture(256, 256,
				TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		FontFactory.setAssetBasePath("fonts/");
		this.mfont = FontFactory.createFromAsset(this.mFontTexture, this,
				"JOKERMAN.TTF", 50, true, Color.RED);
		this.mEngine.getTextureManager().loadTexture(this.mFontTexture);
		this.mEngine.getFontManager().loadFont(this.mfont);

		this.mMenuBGTexture = new Texture(2048, 1024,
				TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		this.mMenuBGTextureRegion = TextureRegionFactory.createFromAsset(
				mMenuBGTexture, this, "menu/menu_bg.jpg", 0, 0);
		this.mEngine.getTextureManager().loadTexture(mMenuBGTexture);

		this.mExitTexture = new Texture(256, 256,
				TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		this.mExitTextureRegion = TextureRegionFactory.createFromAsset(
				mExitTexture, this, "menu/menu_exit.png", 0, 0);
		this.mEngine.getTextureManager().loadTexture(mExitTexture);
	}

	protected void createStaticMenuScene() {
		this.mStaticMenuScene = new MenuScene(this.mCamera);
		final IMenuItem playMenuItem = new ColorMenuItemDecorator(
				new TextMenuItem(MENU_PLAY, mfont, "Play Game"), 0.5f, 0.5f,
				0.5f, 1.0f, 0.0f, 0.0f);
		playMenuItem.setBlendFunction(GL10.GL_SRC_ALPHA,
				GL10.GL_ONE_MINUS_SRC_ALPHA);
		this.mStaticMenuScene.addMenuItem(playMenuItem);

		final IMenuItem scoresMenuItem = new ScaleMenuItemDecorator(
				new TextMenuItem(MENU_SCORES, mfont, "Scores"), 1.2f, 1.0f);
		scoresMenuItem.setBlendFunction(GL10.GL_SRC_ALPHA,
				GL10.GL_ONE_MINUS_SRC_ALPHA);
		this.mStaticMenuScene.addMenuItem(scoresMenuItem);

		final IMenuItem optionsMenuItem = new ColorMenuItemDecorator(
				new ScaleMenuItemDecorator(new TextMenuItem(MENU_OPTIONS,
						mfont, "Options"), 1.2f, 1.0f), 0.5f, 0.5f, 0.5f, 1.0f,
				1.0f, 1.0f);
		optionsMenuItem.setBlendFunction(GL10.GL_SRC_ALPHA,
				GL10.GL_ONE_MINUS_SRC_ALPHA);
		this.mStaticMenuScene.addMenuItem(optionsMenuItem);

		final IMenuItem aboutMenuItem = new TextMenuItem(MENU_ABOUT, mfont,
				"Abuot");
		aboutMenuItem.setBlendFunction(GL10.GL_SRC_ALPHA,
				GL10.GL_ONE_MINUS_SRC_ALPHA);
		this.mStaticMenuScene.addMenuItem(aboutMenuItem);

		final IMenuItem exitMenuItem = new ScaleMenuItemDecorator(
				new SpriteMenuItem(MENU_EXIT, mExitTextureRegion), 1.2f, 1.0f);
		exitMenuItem.setBlendFunction(GL10.GL_SRC_ALPHA,
				GL10.GL_ONE_MINUS_SRC_ALPHA);
		this.mStaticMenuScene.addMenuItem(exitMenuItem);

		this.mStaticMenuScene.buildAnimations();
		this.mStaticMenuScene.setBackgroundEnabled(false);
		this.mStaticMenuScene.setOnMenuItemClickListener(this);
	}

	@Override
	public Scene onLoadScene() {
		// TODO Auto-generated method stub
		this.mEngine.registerUpdateHandler(new FPSLogger());
		this.createStaticMenuScene();
		final int centerX = (CAMERA_WIDTH - this.mMenuBGTextureRegion
				.getWidth()) / 2;
		final int centerY = (CAMERA_HEIGHT - this.mMenuBGTextureRegion
				.getHeight()) / 2;
		this.mMenuScene = new Scene(1);

		final Sprite menubg = new Sprite(centerX, centerY, mMenuBGTextureRegion);
		mMenuScene.getLastChild().attachChild(menubg);
		mMenuScene.setChildScene(mStaticMenuScene);

		return this.mMenuScene;
	}

	@Override
	public boolean onMenuItemClicked(MenuScene pMenuScene, IMenuItem pMenuItem,
			float pMenuItemLocalX, float pMenuItemLocalY) {
		// TODO Auto-generated method stub
		switch (pMenuItem.getID()) {
		case MENU_PLAY:
			this.mMenuScene.registerEntityModifier(new ScaleAtModifier(0.5f, 1.0f, 0.0f,CAMERA_WIDTH/2,CAMERA_HEIGHT/2)
					);
					this.mStaticMenuScene.registerEntityModifier(new ScaleAtModifier(0.5f, 1.0f, 0.0f,CAMERA_WIDTH/2,CAMERA_HEIGHT/2)
							);
					mHandler.postDelayed(mGameLaunch, 1000);
			return true;

		case MENU_SCORES:
			Toast.makeText(StartActivity.this, "scores selected",
					Toast.LENGTH_SHORT).show();
			return true;

		case MENU_OPTIONS:
			Toast.makeText(StartActivity.this, "options selected",
					Toast.LENGTH_SHORT).show();
			return true;

		case MENU_ABOUT:
			Toast.makeText(StartActivity.this, "about selected",
					Toast.LENGTH_SHORT).show();
			return true;

		default:
			return false;
		}
	}
	
	private Runnable mGameLaunch=new Runnable() {
		
		@Override
		public void run() {
			// TODO Auto-generated method stub
			Intent myIntent = new Intent(StartActivity.this, GameActivity.class);
    		StartActivity.this.startActivity(myIntent);
			
		}
	};
}
