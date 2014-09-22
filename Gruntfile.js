module.exports = function(grunt){

  var JSModules = [];

  grunt.file.recurse(
    "./public/javascripts",
    function(abspath, rootdir, subdir, filename) {

      if (
        filename.match(/\.js/) &&
        !filename.match(/application\.js/) &&
        !filename.match(/\.min\.js/) &&
        !filename.match(/\.map/)
      ) JSModules.push(abspath);

    }
  );

  grunt.initConfig({

    // Builds SASS
    sass: {
      dev: {
        options: {
          style: "expanded",
          sourcemap: true,
          includePaths: [
            'govuk_modules/govuk_template/assets/stylesheets',
            'govuk_modules/govuk_frontend_toolkit/stylesheets'
          ]
        },
        files: [{
          expand: true,
          cwd: "app/assets/sass",
          src: ["*.scss"],
          dest: "public/stylesheets/.temp", // Put stylesheet in temp place for other tasks to work on
          ext: ".css"
        }]
      },
      production: {
        options: {
          style: "compressed",
          sourceMap: false,
          includePaths: [
            'govuk_modules/govuk_template/assets/stylesheets',
            'govuk_modules/govuk_frontend_toolkit/stylesheets'
          ]
        },
        files: [{
          expand: true,
          cwd: "app/assets/sass",
          src: ["*.scss"],
          dest: "public/stylesheets",
          ext: ".css"
        }]
      }
    },

    // Compress image assets and move them into public
    imagemin: {
      dynamic: {
        files: [{
          expand: true,
          cwd: "app/assets/images/",
          src: ["*.{png,jpg,gif}"],
          dest: "public/images/"
        }]
      }
    },

    // Make data URIs from images
    dataUri: {
      dist: {
        src: [
          "public/stylesheets/.temp/application.css"
        ],
        dest: "public/stylesheets/",
        options: {
          target: ["public/images/**/*"],
          fixDirLevel: true,
          baseDir: "public/images/"
        }
      }
    },

    // Copies templates and assets from external modules and dirs
    copy: {

      govuk_frontend_toolkit: {
        cwd: 'node_modules/govuk_frontend_toolkit/govuk_frontend_toolkit',
        src: '**',
        dest: 'govuk_modules/govuk_frontend_toolkit/',
        expand: true
      },

      govuk_template: {
        cwd: 'node_modules/govuk_template_mustache/assets/',
        src: '**',
        dest: 'govuk_modules/govuk_template/',
        expand: true
      },

      assets_js: {
        cwd: 'app/assets/javascripts/',
        src: '**',
        dest: 'public/javascripts/',
        expand: true
      },

      toolkit_js: {
        cwd: 'node_modules/govuk_frontend_toolkit/govuk_frontend_toolkit/javascripts/govuk/',
        src: 'selection-buttons.js',
        dest: 'public/javascripts/',
        expand: true
      },

      template_js: {
        cwd: 'node_modules/govuk_template_mustache/assets/javascripts/',
        src: '**',
        dest: 'public/javascripts/',
        expand: true
      },

      toolkit_images: {
        cwd: 'node_modules/govuk_frontend_toolkit/images/',
        src: '**',
        dest: 'public/images/',
        expand: true
      },

      template_images: {
        cwd: 'node_modules/govuk_template_mustache/assets/images/',
        src: '**',
        dest: 'public/images/',
        expand: true
      }
    },

    uglify: {
      dev: {
        options: {
          sourceMap: true,
          sourceMapName: "public/javascripts/sourcemap.map",
          mangle: false,
          compress: false,
          beautify: true,
          preserveComments: true
        },
        files: {
          "public/javascripts/application.js": JSModules
        }
      },
      production: {
        options: {
          sourceMap: false,
          mangle: true,
          compress: true,
          preserveComments: false
        },
        files: {
          "public/assets/javascripts/application.js": JSModules
        }
      }
    },

    // workaround for libsass
    replace: {
      fixSass: {
        src: ['govuk_modules/govuk_template/**/*.scss', 'govuk_modules/govuk_frontend_toolkit/**/*.scss'],
        overwrite: true,
        replacements: [{
          from: /filter:chroma(.*);/g,
          to: 'filter:unquote("chroma$1");'
        }]
      }
    },

    // Remove temporary CSS files
    clean: {
      tempCSS: ['public/stylesheets/.temp']
    },

    // Update whenever CSS/JS/Gruntfile is changed
    watch: {
      css: {
        files: ['app/assets/sass/**/*.scss'],
        tasks: ['sass', 'dataUri'],
        options: { nospawn: true }
      },
      images: {
        files: ['app/assets/images/**/*'],
        tasks: ['imagemin', 'sass'],
        options: { nospawn: true }
      },
      js: {
        files: ['app/assets/javascripts/**/*'],
        tasks: ['copy:assets_js']
      },
      self: {
        files: ['Gruntfile.js'],
        tasks: ['dev']
      }
    },

    // Using Bower to install front-end dependencies
    bower: {
      install: {
        dest: 'public/javascripts',
        options: {
          cleanBowerDir: true,
          stripAffix: true
        }
      }
    }

  });

  // Automatically loads any grunt-* tasks in your package.json
  require("load-grunt-tasks")(grunt);

  grunt.registerTask('dev', [
    'copy',
    'bower',
    'replace',
    'imagemin',
    'sass:dev',
    'dataUri',
    'uglify:dev'
  ]);

  grunt.registerTask('production', [
    'copy',
    'bower',
    'replace',
    'imagemin',
    'sass:production',
    'dataUri',
    'uglify:production'
  ]);

  grunt.registerTask('default', [
    'dev',
    'watch'
  ]);

};
