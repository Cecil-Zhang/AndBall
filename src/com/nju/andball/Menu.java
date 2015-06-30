package com.nju.andball;

import java.io.IOException;
import java.io.InputStream;

import org.andengine.audio.music.Music;
import org.andengine.audio.music.MusicFactory;
import org.andengine.audio.sound.SoundFactory;
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
import org.andengine.entity.text.Text;
import org.andengine.entity.util.FPSLogger;
import org.andengine.input.touch.TouchEvent;
import org.andengine.opengl.font.Font;
import org.andengine.opengl.font.FontFactory;
import org.andengine.opengl.texture.ITexture;
import org.andengine.opengl.texture.TextureOptions;
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
import org.andengine.util.modifier.ease.EaseBackInOut;
import org.andengine.util.modifier.ease.EaseBounceIn;
import org.andengine.util.modifier.ease.EaseBounceInOut;
import org.andengine.util.modifier.ease.EaseBounceOut;
import org.andengine.util.modifier.ease.EaseQuadIn;
import org.andengine.util.modifier.ease.EaseQuadInOut;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.hardware.Camera.Face;
import android.opengl.GLES20;
import android.util.Log;
import android.widget.Toast;

/**
 * (c) Zynga 2011
 *
 * @author Nicolas Gramlich <ngramlich@zynga.com>
 * @since 16:55:18 - 06.11.2011
 */

public class Menu extends SimpleBaseGameActivity implements OnClickListener {
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

	private ITextureRegion mFace10TextureRegion;
	private ITextureRegion mFace11TextureRegion;

	private Sprite face;
	private Sprite monkey;
	private Sprite titleLeft, titleRight;
	private ButtonSprite muteButton;
	private ButtonSprite unmuteButton;

	private Text bestScores;
	private Font mFont;
	private Music mBackgroundMusic;
	private boolean soundEnabled;

	private static final int LAYER_COUNT = 4;

	private static final int LAYER_LOGO = 2;
	private static final int LAYER_BACKGROUND = 1;
	private static final int LAYER_MONKEY = 0;

	private Scene scene;

	@Override
	public EngineOptions onCreateEngineOptions() {
		final Camera camera = new Camera(0, 0, Menu.CAMERA_WIDTH,
				Menu.CAMERA_HEIGHT);

		EngineOptions engineOptions = new EngineOptions(true,
				ScreenOrientation.LANDSCAPE_FIXED, new RatioResolutionPolicy(
						CAMERA_WIDTH, CAMERA_HEIGHT), camera);
		engineOptions.getAudioOptions().setNeedsSound(true);
		engineOptions.getAudioOptions().setNeedsMusic(true);
		if (Constants.getInstance(this).getSoundEnabled()) {
			this.soundEnabled = true;
		} else {
			this.soundEnabled = false;
		}
		Toast.makeText(this, "点击悟空开始游戏", Toast.LENGTH_LONG).show();
		return engineOptions;
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
			public void onDrawFrame(final GLState pGLState)
					throws InterruptedException {
				final boolean firstFrame = !this.mRenderTextureInitialized;

				if (firstFrame) {
					this.initRenderTextures(pGLState);
					this.mRenderTextureInitialized = true;
				}

				final int surfaceWidth = this.mCamera.getSurfaceWidth();
				final int surfaceHeight = this.mCamera.getSurfaceHeight();

				final int currentRenderTextureIndex = this.mCurrentRenderTextureIndex;
				final int otherRenderTextureIndex = (currentRenderTextureIndex + 1)
						% RENDERTEXTURE_COUNT;

				this.mRenderTextures[currentRenderTextureIndex].begin(pGLState,
						false, true);
				{
					/* Draw current frame. */
					super.onDrawFrame(pGLState);

					/* Draw previous frame with reduced alpha. */
					if (!firstFrame) {
						if (Menu.this.mMotionStreaking) {
							this.mRenderTextureSprites[otherRenderTextureIndex]
									.setAlpha(0.9f);
							this.mRenderTextureSprites[otherRenderTextureIndex]
									.onDraw(pGLState, this.mCamera);
						}
					}
				}
				this.mRenderTextures[currentRenderTextureIndex].end(pGLState);

				/* Draw combined frame with full alpha. */
				{
					pGLState.pushProjectionGLMatrix();
					pGLState.orthoProjectionGLMatrixf(0, surfaceWidth, 0,
							surfaceHeight, -1, 1);
					{
						this.mRenderTextureSprites[otherRenderTextureIndex]
								.setAlpha(1);
						this.mRenderTextureSprites[otherRenderTextureIndex]
								.onDraw(pGLState, this.mCamera);
					}
					pGLState.popProjectionGLMatrix();
				}

				/* Flip RenderTextures. */
				this.mCurrentRenderTextureIndex = otherRenderTextureIndex;
			}

			private void initRenderTextures(final GLState pGLState) {
				final int surfaceWidth = this.mCamera.getSurfaceWidth();
				final int surfaceHeight = this.mCamera.getSurfaceHeight();

				final VertexBufferObjectManager vertexBufferObjectManager = this
						.getVertexBufferObjectManager();
				for (int i = 0; i <= 1; i++) {
					this.mRenderTextures[i] = new RenderTexture(
							Menu.this.getTextureManager(), surfaceWidth,
							surfaceHeight);
					this.mRenderTextures[i].init(pGLState);

					final ITextureRegion renderTextureATextureRegion = TextureRegionFactory
							.extractFromTexture(this.mRenderTextures[i]);
					this.mRenderTextureSprites[i] = new Sprite(0, 0,
							renderTextureATextureRegion,
							vertexBufferObjectManager);
				}
			}
		};
	}

	@Override
	public void onCreateResources() {
		try {
			this.mTexture = new BitmapTexture(this.getTextureManager(),
					new IInputStreamOpener() {
						@Override
						public InputStream open() throws IOException {
							return getAssets().open("background2.png");
						}
					});

			this.mTexture.load();
			this.mFaceTextureRegion = TextureRegionFactory
					.extractFromTexture(this.mTexture);

			this.monkeyTexture = new BitmapTexture(this.getTextureManager(),
					new IInputStreamOpener() {
						@Override
						public InputStream open() throws IOException {
							return getAssets().open("monkey.png");
						}
					});

			this.monkeyTexture.load();
			this.mMonkeyTextureRegion = TextureRegionFactory
					.extractFromTexture(this.monkeyTexture);

			this.titleLeftTexture = new BitmapTexture(this.getTextureManager(),
					new IInputStreamOpener() {
						@Override
						public InputStream open() throws IOException {
							return getAssets().open("title-left.png");
						}
					});

			this.titleLeftTexture.load();
			this.titleLeftTextureRegion = TextureRegionFactory
					.extractFromTexture(this.titleLeftTexture);

			this.titleRightTexture = new BitmapTexture(
					this.getTextureManager(), new IInputStreamOpener() {
						@Override
						public InputStream open() throws IOException {
							return getAssets().open("title-right.png");
						}
					});

			this.titleRightTexture.load();
			this.titleRightTextureRegion = TextureRegionFactory
					.extractFromTexture(this.titleRightTexture);

		} catch (IOException e) {
			Debug.e(e);
		}

		BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("sprite/");

		this.mBitmapTextureAtlas = new BuildableBitmapTextureAtlas(
				this.getTextureManager(), 512, 512);

		this.mFace1TextureRegion = BitmapTextureAtlasTextureRegionFactory
				.createFromAsset(this.mBitmapTextureAtlas, this, "start-70.png");
		this.mFace2TextureRegion = BitmapTextureAtlasTextureRegionFactory
				.createFromAsset(this.mBitmapTextureAtlas, this,
						"start2-70.png");
		this.mFace3TextureRegion = BitmapTextureAtlasTextureRegionFactory
				.createFromAsset(this.mBitmapTextureAtlas, this, "start-70.png");

		this.mFace4TextureRegion = BitmapTextureAtlasTextureRegionFactory
				.createFromAsset(this.mBitmapTextureAtlas, this, "aboutUs.png");
		this.mFace5TextureRegion = BitmapTextureAtlasTextureRegionFactory
				.createFromAsset(this.mBitmapTextureAtlas, this, "aboutUs2.png");
		this.mFace6TextureRegion = BitmapTextureAtlasTextureRegionFactory
				.createFromAsset(this.mBitmapTextureAtlas, this, "aboutUs.png");

		this.mFace10TextureRegion = BitmapTextureAtlasTextureRegionFactory
				.createFromAsset(this.mBitmapTextureAtlas, this, "mute.png");
		this.mFace11TextureRegion = BitmapTextureAtlasTextureRegionFactory
				.createFromAsset(this.mBitmapTextureAtlas, this, "unmute.png");

		try {
			this.mBitmapTextureAtlas
					.build(new BlackPawnTextureAtlasBuilder<IBitmapTextureAtlasSource, BitmapTextureAtlas>(
							0, 0, 0));
			this.mBitmapTextureAtlas.load();
		} catch (TextureAtlasBuilderException e) {
			Debug.e(e);
		}

		FontFactory.setAssetBasePath("fonts/");
		this.mFont = FontFactory.createFromAsset(this.getFontManager(),
				this.getTextureManager(), 512, 512, TextureOptions.BILINEAR,
				this.getAssets(), "DeadSpaceTitleFont.ttf", 32, true,
				Color.WHITE);
		this.mFont.load();

		MusicFactory.setAssetBasePath("music/");
		try {
			this.mBackgroundMusic = MusicFactory.createMusicFromAsset(
					this.mEngine.getMusicManager(), this, "startup.mp3");
			this.mBackgroundMusic.setLooping(true);
		} catch (final IOException e) {
			Debug.e(e);
		}
	}

	@Override
	public Scene onCreateScene() {
		this.mEngine.registerUpdateHandler(new FPSLogger());

		/* Create a nice scene with some rectangles. */
		scene = new Scene();

		for (int i = 0; i < LAYER_COUNT; i++) {
			scene.attachChild(new Entity());
		}

		/*
		 * Calculate the coordinates for the face, so its centered on the
		 * camera.
		 */
		final float centerX = (CAMERA_WIDTH - this.mFaceTextureRegion
				.getWidth()) / 2;
		final float centerY = (CAMERA_HEIGHT - this.mFaceTextureRegion
				.getHeight()) / 2;

		final float monkeyX = (CAMERA_WIDTH - this.mMonkeyTextureRegion
				.getWidth()) / 2 - this.mMonkeyTextureRegion.getWidth();
		final float monkeyY = (CAMERA_WIDTH - this.mMonkeyTextureRegion
				.getHeight())
				/ 2
				- this.mMonkeyTextureRegion.getHeight()
				* 2.3f;

		this.mFace1TextureRegion
				.getWidth();
		this.mFace1TextureRegion
				.getHeight();
		this.mFace1TextureRegion.getHeight();

		this.mFace4TextureRegion
				.getWidth();
		this.mFace4TextureRegion
				.getHeight();
		this.mFace4TextureRegion.getHeight();


		final float titleLeftWidth = this.titleLeftTextureRegion.getWidth();
		this.titleLeftTextureRegion.getHeight();
		final float titleRightWidth = this.titleRightTextureRegion.getWidth();
		this.titleRightTextureRegion.getHeight();

		final float titleLeftX = (CAMERA_WIDTH - this.titleLeftTextureRegion
				.getWidth()) / 2 - titleLeftWidth / 2;
		final float titleLeftY = (CAMERA_HEIGHT - this.titleLeftTextureRegion
				.getHeight()) / 2;

		final float titleRightX = (CAMERA_WIDTH - this.titleRightTextureRegion
				.getWidth()) / 2 + titleRightWidth / 2;
		final float titleRightY = (CAMERA_HEIGHT - this.titleRightTextureRegion
				.getHeight()) / 2;

		final float muteX = 58;
		final float muteY = CAMERA_HEIGHT - 68;

		titleLeft = new Sprite(titleLeftX, titleLeftY, titleLeftTextureRegion,
				this.getVertexBufferObjectManager());
		titleRight = new Sprite(titleRightX, titleRightY,
				titleRightTextureRegion, this.getVertexBufferObjectManager());
		titleLeft.setAlpha(0);
		titleRight.setAlpha(0);

		muteButton = new ButtonSprite(muteX, muteY, mFace10TextureRegion,
				this.getVertexBufferObjectManager(), this);
		unmuteButton = new ButtonSprite(muteX, muteY, mFace11TextureRegion,
				this.getVertexBufferObjectManager(), this);
		if (this.soundEnabled) {
			muteButton.setAlpha(0);
			scene.registerTouchArea(unmuteButton);
		} else {
			unmuteButton.setAlpha(0);
			scene.registerTouchArea(muteButton);
		}

		muteButton.registerEntityModifier(new MoveModifier(5, muteX, muteX, 0,
				muteY, EaseBounceOut.getInstance()));
		unmuteButton.registerEntityModifier(new MoveModifier(5, muteX, muteX,
				0, muteY, EaseBounceOut.getInstance()));
		scene.getChildByIndex(LAYER_LOGO).attachChild(unmuteButton);
		scene.getChildByIndex(LAYER_LOGO).attachChild(muteButton);

		monkey = new ButtonSprite(monkeyX, monkeyY, mMonkeyTextureRegion,
				this.getVertexBufferObjectManager(), this);
		monkey.setScale(0);
		face = new Sprite(centerX, centerY, this.mFaceTextureRegion,
				this.getVertexBufferObjectManager());
		face.registerEntityModifier(new SequenceEntityModifier(
				new IEntityModifierListener() {
					@Override
					public void onModifierStarted(
							final IModifier<IEntity> pModifier,
							final IEntity pItem) {
						Menu.this.runOnUiThread(new Runnable() {
							@Override
							public void run() {

							}
						});
					}

					@Override
					public void onModifierFinished(
							final IModifier<IEntity> pEntityModifier,
							final IEntity pEntity) {
						Menu.this.runOnUiThread(new Runnable() {
							@Override
							public void run() {

								monkey.registerEntityModifier(new ParallelEntityModifier(
										new ScaleModifier(1, 0, 1),
										new RotationByModifier(1, -50)));
								scene.getChildByIndex(LAYER_MONKEY)
										.attachChild(monkey);

								titleLeft
										.registerEntityModifier(new ParallelEntityModifier(
												new MoveModifier(1,
														0 - titleLeftWidth,
														titleLeftX, titleLeftY,
														titleLeftY,
														EaseBounceIn
																.getInstance()),
												new AlphaModifier(1, 0, 1)));
								scene.getChildByIndex(LAYER_LOGO).attachChild(
										titleLeft);

								titleRight
										.registerEntityModifier(new ParallelEntityModifier(
												new MoveModifier(
														1,
														CAMERA_WIDTH
																+ titleRightWidth,
														titleRightX,
														titleRightY,
														titleRightY,
														EaseBounceIn
																.getInstance()),
												new AlphaModifier(1, 0, 1),
												new DelayModifier(1.5f)));
								scene.getChildByIndex(LAYER_LOGO).attachChild(
										titleRight);
							}
						});
					}
				},
				new RotationModifier(5, 0, 3600, EaseQuadInOut.getInstance()),
				new DelayModifier(1)));

		scene.getChildByIndex(LAYER_BACKGROUND).attachChild(face);
		scene.registerTouchArea(monkey);
		scene.setTouchAreaBindingOnActionDownEnabled(true);


		// 初始化文本
		float y = CAMERA_HEIGHT * 9 / 10;
		String best = "BestScore: "
				+ Constants.getInstance(this).getHighScore();
		this.bestScores = new Text(CAMERA_WIDTH * 6 / 10, y, this.mFont, best,
				best.length(), this.getVertexBufferObjectManager());
		this.bestScores.setBlendFunction(GLES20.GL_SRC_ALPHA,
				GLES20.GL_ONE_MINUS_SRC_ALPHA);
		this.bestScores.registerEntityModifier(new MoveModifier(5, CAMERA_WIDTH
				- bestScores.getWidth(), CAMERA_WIDTH - bestScores.getWidth(),
				0, y, EaseBounceOut.getInstance()));
		scene.getChildByIndex(LAYER_LOGO).attachChild(bestScores);
		if (this.soundEnabled) {
			this.mBackgroundMusic.play();
		}
		return scene;
	}


	@Override
	public void onBackPressed() {
		this.finish();
	}

	@Override
	public void onPauseGame() {
		super.onPauseGame();
		this.finish();
	}

	// ===========================================================
	// Methods
	// ===========================================================

	@Override
	public void onClick(final ButtonSprite pButtonSprite,
			final float pTouchAreaLocalX, final float pTouchAreaLocalY) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (pButtonSprite.equals(muteButton)) {
					face.registerEntityModifier(new SequenceEntityModifier(
							new RotationModifier(5, 0, 3600, EaseQuadInOut
									.getInstance()), new DelayModifier(2)));
					muteButton.setAlpha(0);
					unmuteButton.setAlpha(1);
					scene.unregisterTouchArea(muteButton);
					scene.registerTouchArea(unmuteButton);
					Constants.getInstance(Menu.this).setSoundEnabled(true);
					mBackgroundMusic.play();
				} else if (pButtonSprite.equals(unmuteButton)) {
					face.registerEntityModifier(new SequenceEntityModifier(
							new RotationModifier(5, 0, 3600, EaseQuadInOut
									.getInstance()), new DelayModifier(2)));
					unmuteButton.setAlpha(0);
					muteButton.setAlpha(1);
					scene.unregisterTouchArea(unmuteButton);
					scene.registerTouchArea(muteButton);
					Constants.getInstance(Menu.this).setSoundEnabled(false);
					mBackgroundMusic.stop();
					Log.i("click", "unmute");
				} else{
					IEntityModifierListener listener = new IEntityModifierListener(){

						@Override
						public void onModifierStarted(
								IModifier<IEntity> pModifier, IEntity pItem) {
							// TODO Auto-generated method stub
							
						}

						@Override
						public void onModifierFinished(
								IModifier<IEntity> pModifier, IEntity pItem) {
							// TODO Auto-generated method stub
							Intent intent = new Intent(Menu.this,PhysicsBall.class);
							startActivity(intent);
							Menu.this.finish();
						}
						
					};
					face.registerEntityModifier(new RotationModifier(3, 0, 3600, listener,EaseQuadInOut
							.getInstance()));
					
				}
			}
		});
	}

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================
}
