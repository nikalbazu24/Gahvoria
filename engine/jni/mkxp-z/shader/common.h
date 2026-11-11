#ifdef GLSLES

#ifdef FRAGMENT_SHADER
/* Only the fragment shader has no default float precision.
 * Essentia: high precision when the GPU has it. With medium precision, big
 * tilesets (old Pokémon Essentials games) are sampled one pixel off, which
 * shows as thin black lines between map tiles. */
#ifdef GL_FRAGMENT_PRECISION_HIGH
precision highp float;
#else
precision mediump float;
#endif
#endif

#else

/* Desktop GLSL doesn't know about these */
#define highp
#define mediump
#define lowp

#endif
