definitions = {
    layout: function () {
        $(this).children().css({'float': 'left'});
        $(this).children().last().css('clear', 'right');
    },
    position: function () {
        if (args == 'fixed') {
            this.style.overflow = 'hidden';
            $(this).next().css('padding-top', '138px');
        }
    },
    textVerticalAlign: function () {
        if (args == 'middle') {
            this.style.display = 'table-cell';
            this.style.verticalAlign = 'middle';
        }
    },
    placement: function () {
        if (args == 'center') {
            $('body').css('margin', '0 auto');
        }
    }
};