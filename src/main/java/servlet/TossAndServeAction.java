package servlet;

import java.util.ArrayList;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import logic.UpdateGameConfigLogic;
import common.JudgeBusinessException;
import common.JudgeSystemException;
import entity.Game;
import entity.GameConfig;
import entity.Regu;

public class TossAndServeAction implements ActionIF {

	public String execute(HttpServletRequest request) {
		String page = "twoMinutesWarmUp.jsp";
		try {
			// パラメータの取得
			String isAreguTossWin = request.getParameter("isAreguTossWin");
			String serve = request.getParameter("serve");
			String court = request.getParameter("court");
			
			ArrayList<String> errorMsgList = new ArrayList<>();
			// パラメータが未入力の場合
			if (isAreguTossWin == null || isAreguTossWin.equals("")) {
				errorMsgList.add("コイントス勝者が未入力です。");
			}
			if (serve == null || serve.equals("")) {
				errorMsgList.add("サーブ権結果が未入力です。");
			}
			if (court == null || court.equals("")) {
				errorMsgList.add("コート権結果が未入力です。");
			}
			if (!errorMsgList.isEmpty()) {
				throw new JudgeBusinessException(errorMsgList);
			}
			
			// セッションから情報取得
			HttpSession session = request.getSession(false);
			if (session == null) {
				throw new JudgeBusinessException("セッションが切れました。再度ログインしてください。");
			}
			Game game = (Game) session.getAttribute("game");
			if (game == null) {
				throw new JudgeBusinessException("セッションが切れました。再度ログインしてください。");
			}

			// // セッションチェック
			boolean result = SessionCheck.checkSession(request, game.getCourtId());
			if (!result) {
				throw new JudgeBusinessException("セッションが切れました。再度ログインしてください。");
			}
			
			// 試合の設定を更新する
			GameConfig gameConfig = new GameConfig();
			gameConfig.setGameId(game.getGameId());
			gameConfig.setIsAreguTossWin(isAreguTossWin.equals("1"));
			gameConfig.setIsAreguFirstServe(serve.equals("1"));
			gameConfig.setIsAreguLeft(court.equals("1"));
			UpdateGameConfigLogic gmLogic = new UpdateGameConfigLogic();
			gmLogic.updateGameConfig(gameConfig);
			session.setAttribute("gameConfig", gameConfig);

			// セッションからAレグとBレグの名前情報を取得する
			Regu reguA = (Regu) session.getAttribute("reguA");
			Regu reguB = (Regu) session.getAttribute("reguB");

			// トス勝者とトス敗者を変数に格納する
			session.setAttribute("tossWinner", isAreguTossWin.equals("1") ? reguA.getName() : reguB.getName());
			session.setAttribute("tossLoser", isAreguTossWin.equals("1") ? reguB.getName() : reguA.getName());
			
		} catch (JudgeBusinessException e) {
			request.setAttribute("errorMsg", e.getMessage());
			request.setAttribute("errorMsgList", e.getMsgList());
			page = "index.jsp";
		}
		catch (JudgeSystemException e) {
			request.setAttribute("errorMsg", e.getMessage());
			page = "systemError.jsp";
		}
		
		return page;
	}

}
