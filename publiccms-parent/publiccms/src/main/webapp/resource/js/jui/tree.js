/**
 * @author Roger Wu
 * @version 1.0 added extend property oncheck
 */
( function($) {
    $.extend($.fn, {
        jTree: function(options) {
            var op = $.extend({
                checkFn: null, selected: "selected", exp: "expandable", coll: "collapsable", firstExp: "first_expandable", firstColl: "first_collapsable",
                lastExp: "last_expandable", lastColl: "last_collapsable", folderExp: "folder_expandable", folderColl: "folder_collapsable", endExp: "end_expandable",
                endColl: "end_collapsable", file: "file", ck: "checked", unck: "unchecked", async: "_src"
            }, options);
            return this.each(function() {
                var $this = $(this);
                var cnum = $this.children().length;
                $(">li", $this).each(function() {
                    var $li = $(this);
                    var first = $li.prev()[0] ? false: true;
                    var last = $li.next()[0] ? false: true;
                    $li.genTree({
                        root: $this,
                        icon: $this.hasClass("treeFolder"), ckbox: $this.hasClass("treeCheck") , excludeParent:  $this.hasClass("excludeParent"), options: op, 
                        exp: ( cnum > 1 ? ( first ? op.firstExp: ( last ? op.lastExp: op.exp ) ): op.endExp ),
                        coll: ( cnum > 1 ? ( first ? op.firstColl: ( last ? op.lastColl: op.coll ) ): op.endColl ),
                        showSub: ( !$this.hasClass("collapse") && ( $this.hasClass("expand") || $li.hasClass("expand") || ( cnum > 1 ? false : true ) ) ),
                        isLast: ( cnum > 1 ? ( last ? true: false ): true )
                    });
                });
                setTimeout(function() {
                    if ($this.hasClass("treeCheck") ) {
                        var checkFn = eval($this.attr("oncheck"));
                        if (checkFn && "function" === typeof checkFn ) {
                            $(".ckbox", $this).each(function() {
                                var ckbox = $(this);
                                ckbox.on("click", function() {
                                    var checked = $(ckbox).hasClass("checked");
                                    var items = [ ];
                                    if (checked ) {
                                        var tnode = $(ckbox).parent().parent();
                                        var boxes = $("input", tnode);
                                        if (boxes.length > 1 ) {
                                            $(boxes).each(function() {
                                                items[items.length] = {
                                                    name: $(this).attr("name"), value: $(this).val()
                                                };
                                            });
                                        } else {
                                            items = {
                                                name: boxes.attr("name"), value: boxes.val()
                                            };
                                        }
                                    }
                                    checkFn({
                                        checked: checked, items: items
                                    });
                                    return false;
                                });
                            });
                        }
                    }
                    $("a", $this).on("click", function(event) {
                        $("div." + op.selected, $this).removeClass(op.selected);
                        var parent = $(this).parent().addClass(op.selected);
                        var $li = $(this).parents("li:first"), sTarget = $li.attr("target");
                        if (sTarget ) {
                            if ($("#" + sTarget, $this).length == 0 ) {
                                $this.prepend("<input id=\"" + sTarget + "\" type=\"hidden\"/>");
                            }
                            $("#" + sTarget, $this).val($li.attr("rel"));
                        }
                        $(".ckbox", parent).trigger("click");
                        event.stopPropagation();
                        $(document).trigger("click");
                        if (!$(this).attr("target") ) {
                            return false;
                        }
                    });
                }, 1);
            });
        },
        subTree: function(op) {
            return this.each(function() {
                $(">li", this).each(function() {
                    var $this = $(this);
                    var isLast = ( $this.next()[0] ? false: true );
                    $this.genTree({
                        root: op.root, icon: op.icon, ckbox: op.ckbox, excludeParent: op.excludeParent, exp: isLast ? op.options.lastExp: op.options.exp, coll: isLast ? op.options.lastColl: op.options.coll,
                        options: op.options, space: isLast ? null: op.space, showSub: op.showSub, isLast: isLast
                    });
                });
            });
        },
        genTree: function(options) {
            var op = $.extend({
                root: options.root, icon: options.icon, ckbox: options.ckbox, excludeParent: options.excludeParent, exp: "", coll: "", showSub: false, options: null, isLast: false
            }, options);
            return this.each(function() {
                var node = $(this);
                var tree = $(">ul", node);
                var parent = node.parent().prev();
                var checked = "unchecked";
                if (op.ckbox && 0 > $(">.checked", parent).length ) {
                    checked = "checked";
                }
                if (tree.length > 0 || node.attr(op.options.async)) {
                    node.children(":first").wrap("<div></div>");
                    var showIcon = op.icon && !tree.hasClass("noFolder");
                    $(">div", node).prepend(( op.ckbox ? "<span class=\"ckbox " + checked + "\"></span>": "" )
                            + ( showIcon ? "<span class=\"" + ( ( op.showSub  && !node.attr(op.options.async) )  ? op.options.folderColl: op.options.folderExp ) + "\"></span>": "<span class=\"" + ( ( op.showSub && !node.attr(op.options.async) ) ? op.coll: op.exp ) + "\"></span>" ));
                    if (tree.length > 0 ) {
                        op.showSub ? tree.show(): tree.hide();
                    }
                    $(">div>."+op.options.folderColl+",>div>."+op.options.folderExp+",>div>a", node).on("click", function() {
                        var tree=$(">ul", node);
                        if(node.attr(op.options.async)){
                            var isHidden = tree.is(":hidden");
                            var treeLength = tree.length;
                            if(treeLength > 0 && isHidden || 0 == treeLength){
                                if(treeLength > 0) {
                                    tree.remove();
                                    tree=$(">ul", node);
                                }
                                $.ajax({
                                    type: "get", url: node.attr(op.options.async), async: false, data: {}, success: function(response){
                                        node.append(response);
                                        tree = $(">ul", node).hide();
                                        initLink(tree);
                                        $("a", tree).on("click", function(event) {
                                            $("div." + op.options.selected, op.root).removeClass(op.options.selected);
                                            var parent = $(this).parent().addClass(op.options.selected);
                                            var $li = $(this).parents("li:first"), sTarget = $li.attr("target");
                                            if (sTarget ) {
                                                if ($("#" + sTarget, op.root).length == 0 ) {
                                                    op.root.prepend("<input id=\"" + sTarget + "\" type=\"hidden\"/>");
                                                }
                                                $("#" + sTarget, op.root).val($li.attr("rel"));
                                            }
                                            $(".ckbox", parent).trigger("click");
                                            event.stopPropagation();
                                            $(document).trigger("click");
                                            if (!$(this).attr("target") ) {
                                                return false;
                                            }
                                        });
                                    },error: JUI.ajaxError
                                });
                            }
                        }
                        var $fnode = $(">li:first", tree);
                        if ($fnode.children(":first").isTag("a") ) {
                            tree.subTree(op);
                        }
                        var isA = $(this).isTag("a");
                        var $this = $(">div>."+op.coll+",>div>."+op.exp, node);
                        if (!isA || tree.is(":hidden") ) {
                            $this.toggleClass(op.exp).toggleClass(op.coll);
                            if (op.icon ) {
                                $(">div>span", node).last().toggleClass(op.options.folderExp).toggleClass(op.options.folderColl);
                            }
                        }
                        ( tree.is(":hidden") ) ? tree.slideDown(100): ( isA ? "": tree.slideUp(100) );
                        return false;
                    });
                    if (op.showSub ) {
                        tree.subTree(op);
                    }
                } else {
                    node.children().wrap("<div></div>");
                    var $box=$(">div", node).prepend(( op.ckbox ? "<span class=\"ckbox " + checked + "\"></span>": "" )
                            + ( op.icon ? "<span class=\""+op.options.file+"\"></span>": "" ));
                    if(node.hasClass(op.options.selected) ){
                        node.removeClass(op.options.selected);
                        $box.addClass(op.options.selected);
                    }
                    if(op.icon ) {
                        $(">div>."+op.options.file, node).on("click", function() {
                            $(this).next().trigger("click");
                            return false;
                        });
                    }
                }
                if (op.ckbox ) {
                    node._check(op);
                }
                $(">div", node).on("click", function() {
                    $("a", this).trigger("click");
                    return false;
                });
            });
        }, _check: function(op) {
            var node = $(this);
            var ckbox = $(">div>.ckbox", node);
            var $input = node.find("a");
            var tname = $input.attr("tname"), tvalue = $input.attr("tvalue");
            var attrs = "";
            if (tname ) {
                attrs += "name=\"" + tname + "\" ";
            }
            if (tvalue ) {
                attrs += "value=\"" + tvalue + "\" ";
            }
            ckbox.append("<input type=\"checkbox\" style=\"display:none;\" " + attrs + "/>").on("click", function() {
                var cked = ckbox.hasClass("checked");
                var aClass = cked ? "unchecked": "checked";
                var rClass = cked ? "checked": "unchecked";
                ckbox.removeClass(rClass).removeClass(!cked ? "indeterminate": "").addClass(aClass);
                $("input", ckbox).prop("checked", !cked);
                $(">ul", node).find("li").each(function() {
                    var box = $(".ckbox", this);
                    box.removeClass(rClass).removeClass(!cked ? "indeterminate": "").addClass(aClass).find("input").prop("checked", !cked);
                });
                $(node)._checkParent(op.excludeParent);
                return false;
            });
            var cAttr = $input.attr("checked") || false;
            if (cAttr ) {
                ckbox.find("input").prop("checked", true);
                ckbox.removeClass("unchecked").addClass("checked");
                $(node)._checkParent(op.excludeParent);
            }
            var cAttr = $input.attr("disabled") || false;
            if (cAttr ) {
                ckbox.find("input").prop("disabled", true);
            }
        }, _checkParent: function(excludeParent) {
            if ($(this).parent().hasClass("tree") ) {
                return;
            }
            var parent = $(this).parent().parent();
            var stree = $(">ul", parent);
            var ckbox = stree.find(">li>a").length + stree.find(".ckbox").length;
            var ckboxed = stree.find(".checked").length;
            var aClass = ( ckboxed == ckbox ? "checked": ( ckboxed != 0 ? "indeterminate": "unchecked" ) );
            var rClass = ( ckboxed == ckbox ? "indeterminate": ( ckboxed != 0 ? "checked": "indeterminate" ) );
            $(">div>.ckbox", parent).removeClass("unchecked").removeClass("checked").removeClass(rClass).addClass(aClass);
            var $checkbox = $(":checkbox", parent);
            if (aClass == "checked" ) {
                $checkbox.prop("checked", true);
                $(">div>.ckbox", parent).find("input").prop("checked", true);
            } else if (aClass == "unchecked" ) {
                $checkbox.removeAttr("checked");
            } else if (aClass == "indeterminate" && !excludeParent) {
                $(">div>.ckbox", parent).find("input").prop("checked", true);
            }
            parent._checkParent(excludeParent);
        }
    });
} )(jQuery);