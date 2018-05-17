;
!
function() {
	layer.use("skin/layer.ext.css", function() {
		layer.layui_layer_extendlayerextjs = !0
	});
	var a = layer.cache || {},
		b = function(b) {
			return a.skin ? " " + a.skin + " " + a.skin + "-" + b : ""
		};
	layer.prompt = function(a, c) {
		a = a || {}, "function" == typeof a && (c = a);
		var d, e = 2 == a.formType ? '<textarea class="layui-layer-input">' + (a.value || "") + "</textarea>" : function() {
				return '<input type="' + (1 == a.formType ? "password" : "text") + '" class="layui-layer-input" value="' + (a.value || "") + '">'
			}();
		return layer.open($.extend({
			btn: ["&#x786E;&#x5B9A;", "&#x53D6;&#x6D88;"],
			content: e,
			skin: "layui-layer-prompt" + b("prompt"),
			success: function(a) {
				d = a.find(".layui-layer-input"), d.focus()
			},
			yes: function(b) {
				var e = d.val();
				"" === e ? d.focus() : e.length > (a.maxlength || 500) ? layer.tips("&#x6700;&#x591A;&#x8F93;&#x5165;" + (a.maxlength || 500) + "&#x4E2A;&#x5B57;&#x6570;", d, {
					tips: 1
				}) : c && c(e, b, d)
			}
		}, a))
	}, layer.tab = function(a) {
		a = a || {};
		var c = a.tab || {};
		return layer.open($.extend({
			type: 1,
			skin: "layui-layer-tab" + b("tab"),
			title: function() {
				var a = c.length,
					b = 1,
					d = "";
				if (a > 0) for (d = '<span class="layui-layer-tabnow">' + c[0].title + "</span>"; a > b; b++) d += "<span>" + c[b].title + "</span>";
				return d
			}(),
			content: '<ul class="layui-layer-tabmain">' +
			function() {
				var a = c.length,
					b = 1,
					d = "";
				if (a > 0) for (d = '<li class="layui-layer-tabli xubox_tab_layer">' + (c[0].content || "no content") + "</li>"; a > b; b++) d += '<li class="layui-layer-tabli">' + (c[b].content || "no  content") + "</li>";
				return d
			}() + "</ul>",
			success: function(a) {
				var b = a.find(".layui-layer-title").children(),
					c = a.find(".layui-layer-tabmain").children();
				b.on("mousedown", function(a) {
					a.stopPropagation ? a.stopPropagation() : a.cancelBubble = !0;
					var b = $(this),
						d = b.index();
					b.addClass("layui-layer-tabnow").siblings().removeClass("layui-layer-tabnow"), c.eq(d).show().siblings().hide()
				})
			}
		}, a))
	}, layer.photos = function(a, c, d) {
		function e(a, b, c) {
			var d = new Image;
			d.onload = function() {
				d.onload = null, b(d)
			}, d.onerror = function(a) {
				d.onerror = null, c(a)
			}, d.src = a
		}
		var f = {};
		if (a = a || {}, a.photos) {
			var g = a.photos.constructor === Object,
				h = g ? a.photos : {},
				i = h.data || [],
				j = h.start || 0;
			if (f.imgIndex = j + 1, g) {
				if (0 === i.length) return void layer.msg("&#x6CA1;&#x6709;&#x56FE;&#x7247;")
			} else {
				var k = $(a.photos),
					l = k.find(a.img || "img");
				if (0 === l.length) return;
				if (c || k.find(h.img || "img").each(function(b) {
					var c = $(this);
					i.push({
						alt: c.attr("alt"),
						pid: c.attr("layer-pid"),
						src: c.attr("layer-src") || c.attr("src"),
						thumb: c.attr("src")
					}), c.on("click", function() {
						layer.photos($.extend(a, {
							photos: {
								start: b,
								data: i,
								tab: a.tab
							},
							full: a.full
						}), !0)
					})
				}), !c) return
			}
			f.imgprev = function(a) {
				f.imgIndex--, f.imgIndex < 1 && (f.imgIndex = i.length), f.tabimg(a)
			}, f.imgnext = function(a, b) {
				f.imgIndex++, f.imgIndex > i.length && (f.imgIndex = 1, b) || f.tabimg(a)
			}, f.keyup = function(a) {
				if (!f.end) {
					var b = a.keyCode;
					a.preventDefault(), 37 === b ? f.imgprev(!0) : 39 === b ? f.imgnext(!0) : 27 === b && layer.close(f.index)
					if(b === 27){
						$('#layui-layer-shade-photo-opt-bg').remove();
            			$('#layui-layer-shade-photo-opt').remove();
					}
				}
			}, f.tabimg = function(b) {
				i.length <= 1 || (h.start = f.imgIndex - 1, layer.close(f.index), layer.photos(a, !0, b))
			}, f.event = function() {
				f.bigimg.hover(function() {
					f.imgsee.show()
				}, function() {
					f.imgsee.hide()
				}), f.bigimg.find(".layui-layer-imgprev").on("click", function(a) {
					a.preventDefault(), f.imgprev()
				}), f.bigimg.find(".layui-layer-imgnext").on("click", function(a) {
					a.preventDefault(), f.imgnext()
				}), $(document).on("keyup", f.keyup)
			}, f.loadi = layer.load(1, {
				shade: "shade" in a ? !1 : .9,
				scrollbar: !1
			}), e(i[j].src, function(c) {
				layer.close(f.loadi), f.index = layer.open($.extend({
					type: 1,
					area: function() {
						var b = [c.width, c.height],
							d = [$(window).width() - 100, $(window).height() - 100];
						return !a.full && b[0] > d[0] && (b[0] = d[0], b[1] = b[0] * d[1] / b[0]), [b[0] + "px", b[1] + "px"]
					}(),
					title: !1,
					shade: .9,
					shadeClose: !0,
					closeBtn: !1,
					move: ".layui-layer-phimg img",
					moveType: 1,
					scrollbar: !1,
					moveOut: !0,
					shift: 5 * Math.random() | 0,
					skin: "layui-layer-photos" + b("photos"),
					content: '<div class="layui-layer-phimg"><img src="' + i[j].src + '" alt="' + (i[j].alt || "") + '" layer-pid="' + i[j].pid + '"><div class="layui-layer-imgsee">' + (i.length > 1 ? '<span class="layui-layer-imguide"><a href="javascript:;" class="layui-layer-iconext layui-layer-imgprev"></a><a href="javascript:;" class="layui-layer-iconext layui-layer-imgnext"></a></span>' : "") + '<div class="layui-layer-imgbar" style="display:' + (d ? "block" : "") + '"><span class="layui-layer-imgtit"><a href="javascript:;">' + (i[j].alt || "") + "</a><em>" + f.imgIndex + "/" + i.length + "</em></span></div></div></div>",
					success: function(b, c) {
						f.bigimg = b.find(".layui-layer-phimg"), f.imgsee = b.find(".layui-layer-imguide,.layui-layer-imgbar"), f.event(b), a.tab && a.tab(i[j], b);
						//添加图片的旋转和放大缩小
			            var div = $('<div>');
			            var divBg = $('<div>');
			            divBg.attr({'id':'layui-layer-shade-photo-opt-bg'});
			            div.attr({'id':'layui-layer-shade-photo-opt'});
			            div.append('<span><i class="fa fa-chevron-left" data-opt="prev"></i></span>')
			            	.append('<span><i class="fa fa-rotate-left" data-opt="rleft"></i></span>')
			            	.append('<span><i class="fa fa-search-plus" data-opt="splus"></i></span>')
			            	.append('<span><i class="fa fa-search-minus" data-opt="sminus"></i></span>')
			            	.append('<span><i class="fa fa-rotate-right" data-opt="rright"></i></span>')
			            	.append('<span><i class="fa fa-chevron-right" data-opt="next"></i></span>');
			            var content = $('.layui-layer-content');
			            var photos = $('.layui-layer-photos');
			            var width = parseInt(photos.css('width').replace('px',''));
			            content.attr('data-rotate','0');
			            photos.attr('data-size','100');
			            //绑定点击事件
			            div.find('i').unbind('click').click(function(){
			            	var $this = $(this);
			            	var opt = $this.attr('data-opt');
			            	var r = parseInt(content.attr('data-rotate'));
			            	var s = parseInt(photos.attr('data-size'));
			            	if(opt == 'rleft'){
				            	content.removeClass('photo'+r);
				            	if((r-90) == -360){
				            		r = 0;
				            	}else{
				            		r -= 90;
				            	}
				            	content.attr('data-rotate',r).addClass('photo'+r);
				            }
				            if(opt == 'rright'){
				            	content.removeClass('photo'+r);
				            	if((r+90) == 360){
				            		r = 0;
				            	}else{
				            		r += 90;
				            	}
				            	content.attr('data-rotate',r).addClass('photo'+r);
				            }
				            if(opt == 'splus'){
				            	if(s<200){
				            		s += 20;
				            	}
				            	photos.attr('data-size',s).css('width',(width*s/100)+'px');
				            }
				            if(opt == 'sminus'){
				            	if(s>20){
				            		s -= 20;
				            	}
				            	photos.attr('data-size',s).css('width',(width*s/100)+'px');
				            }
				            if(opt == 'prev'){
				            	f.imgprev()
				            }
				            if(opt == 'next'){
				            	f.imgnext()
				            }
				            window.layerResize();
			            });
			            $('#layui-layer-shade-photo-opt-bg').remove();
            			$('#layui-layer-shade-photo-opt').remove();
            			if(typeof a.showBtn != 'undefined'){
	            			if(a.showBtn){
	            				$('body').append(divBg).append(div);
	            				$('.layui-layer-phimg img').click(function(){
									window.layerResize();
									setTimeout(function(){
										$('.layui-layer-phimg img').unbind('click');
									},500);
								})
	            			}
            			}else{
            				$('body').append(divBg).append(div);
            			}
					},
					end: function() {
						f.end = !0, $(document).off("keyup", f.keyup);
					}
				}, a))
			}, function() {
				layer.close(f.loadi), layer.msg("&#x5F53;&#x524D;&#x56FE;&#x7247;&#x5730;&#x5740;&#x5F02;&#x5E38;<br>&#x662F;&#x5426;&#x7EE7;&#x7EED;&#x67E5;&#x770B;&#x4E0B;&#x4E00;&#x5F20;&#xFF1F;", {
					time: 3e4,
					btn: ["下一张", "不看了"],
					yes: function() {
						i.length > 1 && f.imgnext(!0, !0)
					}
				})
			})
		}
	}
}();