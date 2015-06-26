package com.nju.andball;

import java.io.IOException;
import java.io.InputStream;

import org.andengine.engine.Engine;
import org.andengine.engine.camera.Camera;
import org.andengine.engine.options.EngineOptions;
import org.andengine.engine.options.ScreenOrientation;
import org.andengine.engine.options.resolutionpolicy.FillResolutionPolicy;
import org.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
import org.andengine.entity.Entity;
import org.andengine.entity.IEntity;
import org.andengine.entity.modifier.AlphaModifier;
import org.andengine.entity.modifier.DelayModifier;
import org.andengine.entity.modifier.LoopEntityModifier;
import org.andengine.entity.modifier.MoveModifier;
import org.andengine.entity.modifier.ParallelEntityModifier;
import org.andengine.entity.modifier.RotationByModifier;
import org.andengine.entity.modifier.RotationModifier;
import org.andengine.entity.modifier.ScaleModifier;
import org.andengine.entity.modifier.SequenceEntityModifier;
import org.andengine.entity.modifier.IEntityModifier.IEntityModifierListener;
import org.andengine.entity.primitive.Rectangle;
import org.andengine.entity.scene.IOnSceneTouchListener;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.sprite.ButtonSprite;
import org.andengine.entity.sprite.ButtonSprite.OnClickListener;
import org.andengine.entity.sprite.Sprite;
import org.andengine.entity.util.FPSLogger;
import org.andengine.input.touch.TouchEvent;
import org.andengine.opengl.texture.ITexture;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
import org.andengine.opengl.texture.atlas.bitmap.BuildableBitmapTextureAtlas;
import org.andengine.opengl.texture.atlas.bitmap.source.IBitmapTextureAtlasSource;
import org.andengine.opengl.texture.atlas.buildable.builder.BlackPawnTextureAtlasBuilder;
import org.andengine.opengl.texture.atlas.buildable.builder.ITextureAtlasBuilder.TextureAtlasBuilderException;
import org.andengine.opengl.texture.bitmap.BitmapTexture;
import org.andengine.opengl.texture.region.ITextureRegion;
import org.andengine.opengl.texture.region.TextureRegionFactory;
import org.andengine.opengl.texture.render.RenderTexture;
import org.andengine.opengl.util.GLState;
import org.andengine.opengl.vbo.VertexBufferObjectManager;
import org.andengine.ui.activity.SimpleBaseGameActivity;
import org.andengine.util.adt.io.in.IInputStreamOpener;
import org.andengine.util.debug.Debug;
import org.andengine.util.modifier.IModifier;
import org.andengine.util.modifier.ease.EaseBounceIn;
import org.andengine.util.modifier.ease.EaseQuadIn;
import org.andengine.util.modifier.ease.EaseQuadInOut;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.hardware.Camera.Face;
import android.widget.Toast;

/**
 * (c) Zynga 2011
 *
 * @author Nicolas Gramlich <ngramlich@zynga.com>
 * @since 16:55:18 - 06.11.2011
 */

public class Menu extends SimpleBaseGameActivity implements IOnSceneTouchListener, OnClickListener {
	// ===========================================================
	// Constants
	// ===========================================================

	private static final int CAMERA_WIDTH = 720;
	private static final int CAMERA_HEIGHT = 480;

	// ===========================================================
	// Fields
	// ===========================================================

	private boolean mMotionStreaking = true;
	
	private ITexture mTexture;
	private ITexture monkeyTexture;
	private ITextureRegion mFaceTextureRegion;
	private ITextureRegion mMonkeyTextureRegion;
	private ITexture titleLeftTexture;
	private ITexture titleRightTexture;
	private ITextureRegion titleLeftTextureRegion;
	private ITextureRegion titleRightTextureRegion;
	
	private BuildableBitmapTextureAtlas mBitmapTextureAtlas;
	private ITextureRegion mFace1TextureRegion;
	private ITextureRegion mFace2TextureRegion;
	private ITextureRegion mFace3TextureRegion;
	private ITextureRegion mFace4TextureRegion;
	private ITextureRegion mFace5TextureRegion;
	private ITextureRegion mFace6TextureRegion;
	private ITextureRegion mFace7TextureRegion;
	private ITextureRegion mFace8TextureRegion;
	private ITextureRegion mFace9TextureRegion;
	
	private Sprite face;
	private Sprite monkey;
	private Sprite titleLeft,titleRight;
	private ButtonSprite face2;
	private ButtonSprite face3;
	private ButtonSprite face4;
	
	private static final int LAYER_COUNT = 4;

	private static final int LAYER_BUTTON=3;
	private static final int LAYER_LOGO=2;
	private static final int LAYER_BACKGROUND = 1;
	private static final int LAYER_MONKEY=0;


	@Override
	public EngineOptions onCreateEngineOptions() {
		final Camera camera = new Camera(0, 0, Menu.CAMERA_WIDTH, Menu.CAMERA_HEIGHT);

		return new EngineOptions(true, ScreenOrientation.LANDSCAPE_FIXED,  new RatioResolutionPolicy(
				CAMERA_WIDTH, CAMERA_HEIGHT), camera);
	}

	@Override
	public Engine onCreateEngine(EngineOptions pEngineOptions) {
		return new Engine(pEngineOptions) {
			private static final int RENDERTEXTURE_COUNT = 2;

			private boolean mRenderTextureInitialized;

			private final RenderTexture[] mRenderTextures = new RenderTexture[RENDERTEXTURE_COUNT];
			private final Sprite[] mRenderTextureSprites = new Sprite[RENDERTEXTURE_COUNT];

			private int mCurrentRenderTextureIndex = 0;

			@Override
			public void onDrawFrame(final GLState pGLState) throws InterruptedException {
				final boolean firstFrame = !this.mRenderTextureInitialized;

				if(firstFrame) {
					this.initRenderTextures(pGLState);
					this.mRenderTextureInitialized = true;
				}

				final int surfaceWidth = this.mCamera.getSurfaceWidth();
				final int surfaceHeight = this.mCamera.getSurfaceHeight();

				final int currentRenderTextureIndex = this.mCurrentRenderTextureIndex;
				final int otherRenderTextureIndex = (currentRenderTextureIndex + 1) % RENDERTEXTURE_COUNT;

				this.mRenderTextures[currentRenderTextureIndex].begin(pGLState, false, true);
				{
					/* Draw current frame. */
					super.onDrawFrame(pGLState);

					/* Draw previous frame with reduced alpha. */
					if(!firstFrame) {
						if(Menu.this.mMotionStreaking) {
							this.mRenderTextureSprites[otherRenderTextureIndex].setAlpha(0.9f);
							this.mRenderTextureSprites[otherRenderTextureIndex].onDraw(pGLState, this.mCamera);
						}
					}
				}
				this.mRenderTextures[currentRenderTextureIndex].end(pGLState);

				/* Draw combined frame with full alpha. */
				{
					pGLState.pushProjectionGLMatrix();
					pGLState.orthoProjectionGLMatrixf(0, surfaceWidth, 0, surfaceHeight, -1, 1);
					{
						this.mRenderTextureSprites[otherRenderTextureIndex].setAlpha(1);
						this.mRenderTextureSprites[otherRenderTextureIndex].onDraw(pGLState, this.mCamera);
					}
					pGLState.popProjectionGLMatrix();
				}

				/* Flip RenderTextures. */
				this.mCurrentRenderTextureIndex = otherRenderTextureIndex;
			}

			private void initRenderTextures(final GLState pGLState) {
				final int surfaceWidth = this.mCamera.getSurfaceWidth();
				final int surfaceHeight = this.mCamera.getSurfaceHeight();

				final VertexBufferObjectManager vertexBufferObjectManager = this.getVertexBufferObjectManager();
				for(int i = 0; i <= 1; i++) {
					this.mRenderTextures[i] = new RenderTexture(Menu.this.getTextureManager(), surfaceWidth, surfaceHeight);
					this.mRenderTextures[i].init(pGLState);

					final ITextureRegion renderTextureATextureRegion = TextureRegionFactory.extractFromTexture(this.mRenderTextures[i]);
					this.mRenderTextureSprites[i] = new Sprite(0, 0, renderTextureATextureRegion, vertexBufferObjectManager);
				}
			}
		};
	}

	@Override
	public void onCreateResources() {
		try {
			this.mTexture = new BitmapTexture(this.getTextureManager(), new IInputStreamOpener() {
				@Override
				public InputStream open() throws IOException {
					return getAssets().open("background2.png");
				}
			});

			this.mTexture.load();
			this.mFaceTextureRegion = TextureRegionFactory.extractFromTexture(this.mTexture);
			
			this.monkeyTexture = new BitmapTexture(this.getTextureManager(), new IInputStreamOpener() {
				@Override
				public InputStream open() throws IOException {
					return getAssets().open("monkey.png");
				}
			});

			this.monkeyTexture.load();
			this.mMonkeyTextureRegion = TextureRegionFactory.extractFromTexture(this.monkeyTexture);
			
			this.titleLeftTexture = new BitmapTexture(this.getTextureManager(), new IInputStreamOpener() {
				@Override
				public InputStream open() throws IOException {
					return getAssets().open("title-left.png");
				}
			});

			this.titleLeftTexture.load();
			this.titleLeftTextureRegion = TextureRegionFactory.extractFromTexture(this.titleLeftTexture);
			
			this.titleRightTexture = new BitmapTexture(this.getTextureManager(), new IInputStreamOpener() {
				@Override
				public InputStream open() throws IOException {
					return getAssets().open("title-right.png");
				}
			});

			this.titleRightTexture.load();
			this.titleRightTextureRegion = TextureRegionFactory.extractFromTexture(this.titleRightTexture);
			
			
		} catch (IOException e) {
			Debug.e(e);
		}
		
		BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/");

		this.mBitmapTextureAtlas = new BuildableBitmapTextureAtlas(this.getTextureManager(), 512, 512);

		this.mFace1TextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mBitmapTextureAtlas, this, "start-70.png");
		this.mFace2TextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mBitmapTextureAtlas, this, "start2-70.png");
		this.mFace3TextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mBitmapTextureAtlas, this, "start-70.png");
		
		this.mFace4TextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mBitmapTextureAtlas, this, "aboutUs.png");
		this.mFace5TextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mBitmapTextureAtlas, this, "aboutUs2.png");
		this.mFace6TextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mBitmapTextureAtlas, this, "aboutUs.png");
		
		this.mFace7TextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mBitmapTextureAtlas, this, "menu_quit.png");
		this.mFace8TextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mBitmapTextureAtlas, this, "menu_quit.png");
		this.mFace9TextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mBitmapTextureAtlas, this, "menu_quit.png");
		
		try {
			this.mBitmapTextureAtlas.build(new BlackPawnTextureAtlasBuilder<IBitmapTextureAtlasSource, BitmapTextureAtlas>(0, 0, 0));
			this.mBitmapTextureAtlas.load();
		} catch (TextureAtlasBuilderException e) {
			Debug.e(e);
		}
	}

	@Override
	public Scene onCreateScene() {
		this.mEngine.registerUpdateHandler(new FPSLogger());

		/* Create a nice scene with some rectangles. */
		final Scene scene = new Scene();
		
		for(int i = 0; i < LAYER_COUNT; i++) {
			scene.attachChild(new Entity());
		}
		
		/* Calculate the coordinates for the face, so its centered on the camera. */
		final float centerX = (CAMERA_WIDTH - this.mFaceTextureRegion.getWidth()) / 2;
		final float centerY = (CAMERA_HEIGHT - this.mFaceTextureRegion.getHeight()) / 2;
		
		final float monkeyX=(CAMERA_WIDTH - this.mMonkeyTextureRegion.getWidth()) / 2 - this.mMonkeyTextureRegion.getWidth();
		final float monkeyY=(CAMERA_WIDTH - this.mMonkeyTextureRegion.getHeight()) / 2 - this.mMonkeyTextureRegion.getHeight()*2.3f;
		
		final float centerX2 = (CAMERA_WIDTH  - this.mFace1TextureRegion.getWidth()) / 2;
		final float centerY2 = (CAMERA_HEIGHT - this.mFace1TextureRegion.getHeight()) / 2-this.mFace1TextureRegion.getHeight()*1.5f;
		
		final float centerX3 = (CAMERA_WIDTH  - this.mFace4TextureRegion.getWidth()) / 2;
		final float centerY3 = (CAMERA_HEIGHT - this.mFace4TextureRegion.getHeight()) / 2-this.mFace4TextureRegion.getHeight()*0.5f;
		
		final float centerX4 = (CAMERA_WIDTH  - this.mFace7TextureRegion.getWidth()) / 2;
		final float centerY4 = (CAMERA_HEIGHT - this.mFace7TextureRegion.getHeight()) / 2+this.mFace7TextureRegion.getHeight()*1;
		
		
		face2 = new ButtonSprite(centerX2, centerY2, this.mFace1TextureRegion, this.mFace2TextureRegion, this.mFace3TextureRegion, this.getVertexBufferObjectManager(), this);
		scene.registerTouchArea(face2);
		face2.setAlpha(0);
		
		face3 = new ButtonSprite(centerX3, centerY3, this.mFace4TextureRegion, this.mFace5TextureRegion, this.mFace6TextureRegion, this.getVertexBufferObjectManager(), this);
		scene.registerTouchArea(face3);
		face3.setAlpha(0);
		
		face4 = new ButtonSprite(centerX4, centerY4, this.mFace7TextureRegion, this.mFace8TextureRegion, this.mFace9TextureRegion, this.getVertexBufferObjectManager(), this);
		scene.registerTouchArea(face4);
		face4.setAlpha(0);
		
		final float titleLeftWidth=this.titleLeftTextureRegion.getWidth();
		final float titleLeftHeight=this.titleLeftTextureRegion.getHeight();
		final float titleRightWidth=this.titleRightTextureRegion.getWidth();
		final float titleRightHeight=this.titleRightTextureRegion.getHeight();
		
		final float titleLeftX = (CAMERA_WIDTH - this.titleLeftTextureRegion.getWidth()) / 2 -titleLeftWidth/2;
		final float titleLeftY = (CAMERA_HEIGHT - this.titleLeftTextureRegion.getHeight()) / 2;
		
		final float titleRightX = (CAMERA_WIDTH - this.titleRightTextureRegion.getWidth()) / 2 + titleRightWidth/2;
		final float titleRightY = (CAMERA_HEIGHT - this.titleRightTextureRegion.getHeight()) / 2;
		
		
		titleLeft=new Sprite(titleLeftX, titleLeftY, titleLeftTextureRegion, this.getVertexBufferObjectManager());
		titleRight=new Sprite(titleRightX, titleRightY, titleRightTextureRegion, this.getVertexBufferObjectManager());
		titleLeft.setAlpha(0);
		titleRight.setAlpha(0);
		
		monkey=new Sprite(monkeyX, monkeyY, mMonkeyTextureRegion, this.getVertexBufferObjectManager());
		monkey.setScale(0);
		face = new Sprite(centerX, centerY, this.mFaceTextureRegion, this.getVertexBufferObjectManager());
		face.registerEntityModifier(
										new SequenceEntityModifier(
												new IEntityModifierListener() {
													@Override
													public void onModifierStarted(final IModifier<IEntity> pModifier, final IEntity pItem) {
														Menu.this.runOnUiThread(new Runnable() {
															@Override
															public void run() {
																
															}
														});
													}

													@Override
													public void onModifierFinished(final IModifier<IEntity> pEntityModifier, final IEntity pEntity) {
														Menu.this.runOnUiThread(new Runnable() {
															@Override
															public void run() {
																
																monkey.registerEntityModifier(
																		new ParallelEntityModifier(
																			new ScaleModifier(1, 0, 1),
																			new RotationByModifier(1, -50)
																		));
																scene.getChildByIndex(LAYER_MONKEY).attachChild(monkey);
																
																
																titleLeft.registerEntityModifier(
																			new ParallelEntityModifier(
																				new MoveModifier(1,0-titleLeftWidth,titleLeftX,titleLeftY,titleLeftY,EaseBounceIn.getInstance()),
																				new AlphaModifier(1, 0, 1)
																			)
																		);
																scene.getChildByIndex(LAYER_LOGO).attachChild(titleLeft);
																
																titleRight.registerEntityModifier(
																		new ParallelEntityModifier(
																			new MoveModifier(1,CAMERA_WIDTH+titleRightWidth,titleRightX,titleRightY,titleRightY,EaseBounceIn.getInstance()),
																			new AlphaModifier(1, 0, 1),
																			new DelayModifier(1.5f)
																		)
																	);
																scene.getChildByIndex(LAYER_LOGO).attachChild(titleRight);
																
																face2.registerEntityModifier(new AlphaModifier(0.7f, 0, 1));
																scene.getChildByIndex(LAYER_BUTTON).attachChild(face2);
																
																face3.registerEntityModifier(new AlphaModifier(1.7f, 0, 1));
																scene.getChildByIndex(LAYER_BUTTON).attachChild(face3);
																
//																face4.registerEntityModifier(new AlphaModifier(2.7f, 0, 1));
//																scene.getChildByIndex(LAYER_BUTTON).attachChild(face4);
//																
																
															}
														});
													}
												},
												new RotationModifier(5, 0, 3600, EaseQuadInOut.getInstance()), 
												new DelayModifier(1)
										)
									);
		
		
		
		scene.getChildByIndex(LAYER_BACKGROUND).attachChild(face);
		
		
		
		
		scene.setTouchAreaBindingOnActionDownEnabled(true);
		
		/* TouchListener */
		//scene.setOnSceneTouchListener(this);

		return scene;
	}

	@Override
	public boolean onSceneTouchEvent(final Scene pScene, final TouchEvent pSceneTouchEvent) {
		if(pSceneTouchEvent.isActionDown()) {
			Menu.this.mMotionStreaking = !Menu.this.mMotionStreaking;

			Menu.this.runOnUiThread(new Runnable(){
				@Override
				public void run() {
					Toast.makeText(Menu.this, "MotionStreaking " + (Menu.this.mMotionStreaking ? "enabled." : "disabled."), Toast.LENGTH_SHORT).show();
				}
			});
		}
		return true;
	}

	// ===========================================================
	// Methods
	// ===========================================================
	
	private Rectangle makeColoredRectangle(final float pX, final float pY, final float pRed, final float pGreen, final float pBlue) {
		final Rectangle coloredRect = new Rectangle(pX, pY, 180, 180, this.getVertexBufferObjectManager());
		coloredRect.setColor(pRed, pGreen, pBlue);
		return coloredRect;
	}

	@Override
	public void onClick(final ButtonSprite pButtonSprite, final float pTouchAreaLocalX, final float pTouchAreaLocalY) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				face.registerEntityModifier(
							new SequenceEntityModifier(
								new RotationModifier(7, 0, 3600, EaseQuadInOut.getInstance()), 
								new DelayModifier(2)
							)
						);
				if(pButtonSprite.equals(face2)){
					Toast.makeText(Menu.this, "开始游戏", Toast.LENGTH_LONG).show();
					Intent intent = new Intent(Menu.this,PhysicsBall.class);
					startActivity(intent);
				}else if(pButtonSprite.equals(face3)){
					Toast.makeText(Menu.this, "关于我们", Toast.LENGTH_LONG).show();
				}
				
				
			}
		});
	}

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================
}
