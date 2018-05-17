/**
 * Created by MAJIANGTAO on 2015/11/11.
 */

define('timeObjectUtil',[],function(){
    return {

        //获取当前时间毫秒
        getCurrentMsTime : function() {
            var myDate = new Date();
            return myDate.getTime();
        },

        //毫秒转时间格式（含时间）
        longMsTimeConvertToDateTime : function(time) {
            var myDate = new Date(time);
            return this.formatterDateTime(myDate);
        },

        //毫秒转成时间格式（不含时间）
        longMsTimeConvertToDate : function(time){
            var myDate = new Date(time);
            return this.formatterDate(myDate);
        },

        //毫秒转成时间格式（不含时间）
        longMsTimeConvertToDate2 : function(time){
            var myDate = new Date(time);
            return this.formatterDate2(myDate);
        },

        //毫秒转成时间格式（不含时间）
        longMsTimeConvertToDate3 : function(time){
            var myDate = new Date(time);
            return this.formatterDate3(myDate);
        },

        //格式化日期（不含时间)
        formatterDate : function(date) {
            var datetime = //date.getFullYear()
                //+ "-"// "月"
                ((date.getMonth() + 1) > 9 ? (date.getMonth() + 1) : "0" + (date.getMonth() + 1))
                + "-"// "日"
                + (date.getDate() < 10 ? "0" + date.getDate() : date.getDate());
            return datetime;
        },

        //格式化日期（含时间"00:00:00"）
        formatterDate2 : function(date) {
            var datetime = date.getFullYear()
                + "-"// "月"
                + ((date.getMonth() + 1) > 9 ? (date.getMonth() + 1) : "0" + (date.getMonth() + 1))
                + "-"// "日期"
                + (date.getDate() < 10 ? "0" + date.getDate() : date.getDate()) + " " + "00:00:00";
            return datetime;
        },

        //格式化日
        formatterDate3 : function(date) {
            var datetime = date.getFullYear()
                + "-"// "月"
                + ((date.getMonth() + 1) > 9 ? (date.getMonth() + 1) : "0" + (date.getMonth() + 1))
                + "-"// "日期"
                + (date.getDate() < 10 ? "0" + date.getDate() : date.getDate());
            return datetime;
        },

        //格式化去日期（含时间）
        formatterDateTime : function(date) {
            var datetime = date.getFullYear()
                + "-"// "月"
                + ((date.getMonth() + 1) > 9 ? (date.getMonth() + 1) : "0" + (date.getMonth() + 1))
                + "-"// "日"
                + (date.getDate() < 10 ? "0" + date.getDate() : date.getDate())
                + " "
                + (date.getHours() < 10 ? "0" + date.getHours() : date.getHours())
                + ":"
                + (date.getMinutes() < 10 ? "0" + date.getMinutes() : date.getMinutes())
                //+ ":"
                //+ (date.getSeconds() < 10 ? "0" + date.getSeconds() : date.getSeconds());
            return datetime;
        },
        //获取过去了多长时
        getLongtime : function(time){
            var now = this.getCurrentMsTime();
            var longtime = now - time;
            var s = longtime/1000,
                m = longtime/1000/60,
                h = longtime/1000/60/60,
                d = longtime/1000/60/60/24;
            if(m>=1 && m<60)
                return parseInt(m)+'分钟前';
            else if(h>=1 && h<24)
                return parseInt(h)+'小时前';
            else if(h>=24)
                return this.longMsTimeConvertToDate3(time);
            else {
                return '刚刚';
            }
        },
        //获取过去多少天
        getLongDays : function (time) {
            var now = this.getCurrentMsTime();
            var longtime = now - time;
            var s = longtime/1000,
                m = longtime/1000/60,
                h = longtime/1000/60/60,
                d = longtime/1000/60/60/24;

            return Math.floor(d) < 0 ? 0 : Math.floor(d);
        },
        //获取还剩多少天
        getSurplusDays : function(time){
            var now = this.getCurrentMsTime();
            var surplustime = time - now;
            var s = surplustime/1000,
                m = surplustime/1000/60,
                h = surplustime/1000/60/60,
                d = surplustime/1000/60/60/24;

            return Math.floor(d) < 0 ? -1 : Math.floor(d);
        },
        //判断时间
        isNowTime : function (time) {
            var is = '';
            var now = this.getCurrentMsTime();
            var stime = this.longMsTimeConvertToDate3(now).replace(new RegExp("-","gm"),"/");
            var ntime = (new Date(stime)).getTime();
            if(time > ntime){
                //console.log('未到期');
                is = 1;
            }else if(time == ntime){
                //console.log('今天');
                is = 0;
            }else{
                //console.log('过期');
                is = -1;
            }
            return is;
        },
        //加上X个月之后的年月日
        getXMonthAfter : function(num){
            var myDate = new Date();
            var myAfterDate=new Date(myDate.getFullYear(),myDate.getMonth()+num,myDate.getDate());
            return myAfterDate.getFullYear()+'年'+(myAfterDate.getMonth()+1)+'月'+myAfterDate.getDate()+'日';
        },
        //获取一段时间的日期
        GetDates: function(begin, end){
            var ab = begin.split("-");
            var ae = end.split("-");
            var db = new Date();
                db.setFullYear(ab[0], ab[1]-1, ab[2]);
            var de = new Date();
                de.setFullYear(ae[0], ae[1]-1, ae[2]);
            var a = [];
            for (var i=0,temp=db;temp <= de;i++)
            {
                var month = (temp.getMonth()+1) > 9 ? (temp.getMonth()+1) : '0'+(temp.getMonth()+1);
                var day = temp.getDate() > 9 ? temp.getDate() : '0'+temp.getDate();
                a[i] = temp.getFullYear() + "-" + month + "-" +  day;
                temp.setTime(temp.getTime() + 24*60*60*1000);
            }
            return a;
        }
    }
});