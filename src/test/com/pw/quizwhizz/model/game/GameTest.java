package com.pw.quizwhizz.model.game;

import com.pw.quizwhizz.model.exception.IllegalNumberOfQuestionsException;
import com.pw.quizwhizz.model.exception.IllegalTimeOfAnswerSubmissionException;
import com.pw.quizwhizz.model.exception.ScoreCannotBeRetrievedBeforeGameIsClosedException;
import org.assertj.core.api.Java6Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Klasa testująca logikę biznesową gry, wykorzystująca obiekty mockujące biblioteki Mockito.
 *
 * @author Karolina Prusaczyk
 * @see Game
 */
@RunWith(MockitoJUnitRunner.class)
public class GameTest {
    private Category category;
    private List<Question> questions;
    private GameStateMachine gameStateMachine;
    private ScoreBuilder scoreBuilder;
    @Mock
    private Player player1;
    @Mock
    private Player player2;
    private List<Score> scores;
    private List<Answer> answersOfP1;
    private List<Answer> answersOfP2;

    @Test
    public void publicConstructorTest() throws IllegalNumberOfQuestionsException {
        givenMockedCategoryQuestionsGameStateMachineAndScoreBuilder();
        given10Questions();

        Game game = new Game(category, questions);

        Java6Assertions.assertThat(game.getCategory()).isEqualTo(category);
        Java6Assertions.assertThat(game.getQuestions()).isEqualTo(questions);
    }

    @Test
    public void givenWrongNumberOfQuestions_WhenCreatingGame_ThenExceptionShouldBeThrown() throws IllegalNumberOfQuestionsException {
        Category category = mock(Category.class);
        List<Question> questions = mock(List.class);
        when(questions.size()).thenReturn(0);

        assertThatThrownBy(() -> new Game(category, questions)).isInstanceOf(IllegalNumberOfQuestionsException.class);
    }

    @Test
    public void givenListContainingPlayer1_WhenAddingPlayer1ToGameAgain_ThenTheyShouldNotBeAdded() throws IllegalNumberOfQuestionsException {
        givenMockedCategoryQuestionsGameStateMachineAndScoreBuilder();
        given10Questions();
        Game game = givenGameWithCategoryQuestionsAndStateMachine();

        game.addPlayer(player1);
        game.addPlayer(player1);

        assertThat(game.getPlayers().size()).isEqualTo(1);
    }

    @Test
    public void givenCategoryQuestionsAndNotClosedGameStatus_WhenCheckScoresIsCalled_ThenScoresAreNotNull() throws IllegalNumberOfQuestionsException, ScoreCannotBeRetrievedBeforeGameIsClosedException {
        givenMockedCategoryQuestionsGameStateMachineAndScoreBuilder();
        given10Questions();
        Game game = givenGameWithCategoryQuestionsAndStateMachine();
        givenGameStateIsClosed();

        scores = game.checkScores();

        Java6Assertions.assertThat(game.getCategory()).isEqualTo(category);
        Java6Assertions.assertThat(game.getQuestions()).isEqualTo(questions);
        verify(gameStateMachine, times(1)).determineCurrentState();
        Java6Assertions.assertThat(scores).isNotNull();
    }

    @Test
    public void givenGameStateIsClosed_WhenGetScoresIsCalled_ThenExceptionIsThrown() throws ScoreCannotBeRetrievedBeforeGameIsClosedException, IllegalNumberOfQuestionsException {
        givenMockedCategoryQuestionsGameStateMachineAndScoreBuilder();
        given10Questions();
        Game game = givenGameWithCategoryQuestionsAndStateMachine();
        givenGameStateIsNotClosed();

        assertThatThrownBy(() -> game.checkScores()).isInstanceOf(ScoreCannotBeRetrievedBeforeGameIsClosedException.class);
    }

    @Test
    public void givenGameIsNotInProgress_WhenEvaluateAnswersIsCalled_ThenExceptionShouldBeThrown() throws IllegalNumberOfQuestionsException {
        givenMockedCategoryQuestionsGameStateMachineAndScoreBuilder();
        given10Questions();
        Game game = givenGameWithCategoryQuestionsAndStateMachine();
        givenTwoPlayersAreAddedToTheGame(game);
        givenGameIsNotInProgress(true);

        assertThatThrownBy(() -> game.evaluateAnswers(player1, answersOfP1)).isInstanceOf(IllegalTimeOfAnswerSubmissionException.class);

    }

    @Test
    public void givenPlayer1sAnswersWereEvaluated_WhenEvaluatingTheSameAnswers_ThenScoreShouldNotBeAddedToScoresAgain() throws IllegalNumberOfQuestionsException, IllegalTimeOfAnswerSubmissionException, ScoreCannotBeRetrievedBeforeGameIsClosedException {
        givenMockedCategoryQuestionsGameStateMachineAndScoreBuilder();
        given10Questions();
        Game game = givenGameWithCategoryQuestionsAndStateMachine();
        Player player1 = mock(Player.class);
        game.addPlayer(player1);

        givenGameIsNotInProgress(false);
        answersOfP1 = givenMockedListOfOneCorrectAnswer();

        game.evaluateAnswers(player1, answersOfP1);
        game.evaluateAnswers(player1, answersOfP1);

        givenGameStateIsClosed();
        assertThat(game.checkScores().size()).isEqualTo(1);

    }

    @Test
    public void given2PlayersAndTheirEvaluatedAnswers_WhenGetScoresIsCalled_ThenProperScoreOfProperPlayerShouldBeMarkedAsHighest() throws IllegalNumberOfQuestionsException, IllegalTimeOfAnswerSubmissionException, ScoreCannotBeRetrievedBeforeGameIsClosedException {

        givenMockedCategoryQuestionsGameStateMachineAndScoreBuilder();
        given10Questions();
        Game game = givenGameWithCategoryQuestionsAndStateMachine();
        givenTwoPlayersAreAddedToTheGame(game);
        givenStartIsCalled(game);
        givenGameIsNotInProgress(false);

        answersOfP1 = givenMockedListOfOneCorrectAnswer();
        answersOfP2 = givenMockedListOfNoCorrectAnswers();

        game.evaluateAnswers(player1, answersOfP1);
        game.evaluateAnswers(player2, answersOfP2);

        givenGameStateIsClosed();

        scores = game.checkScores();

        verify(gameStateMachine, atLeast(2)).determineCurrentState();
        verify(gameStateMachine, times(1)).start();
        assertThat(game.getPlayers()).contains(player1);
        assertThat(game.getPlayers()).contains(player2);
        assertThat(scores.size()).isEqualTo(2);
        assertThat(scores.get(0).isHighest()).isTrue();
        assertThat(scores.get(0).getPlayer()).isEqualTo(player1);
        assertThat(scores.get(1).isHighest()).isFalse();
        assertThat(scores.get(1).getPlayer()).isEqualTo(player2);
    }


    private void givenMockedCategoryQuestionsGameStateMachineAndScoreBuilder() {
        category = mock(Category.class);
        questions = mock(List.class);
        gameStateMachine = mock(GameStateMachine.class);
        scoreBuilder = mock(ScoreBuilder.class);
    }

    private void given10Questions() {
        when(questions.size()).thenReturn(10);
    }

    private Game givenGameWithCategoryQuestionsAndStateMachine() throws IllegalNumberOfQuestionsException {
        return new Game(category, questions, gameStateMachine, scoreBuilder);
    }

    private void givenStartIsCalled(Game game) {
        game.start();
    }

    private void
    givenGameStateIsNotClosed() {
        when(gameStateMachine.gameIsClosed()).thenReturn(false);
    }

    private void givenGameStateIsClosed() {
        when(gameStateMachine.gameIsClosed()).thenReturn(true);
    }

    private void givenGameIsNotInProgress(boolean t) {
        when(gameStateMachine.gameIsNotInProgress()).thenReturn(t);
    }

    private void givenTwoPlayersAreAddedToTheGame(Game game) {
        game.addPlayer(player1);
        game.addPlayer(player2);
    }

    private List<Answer> givenMockedListOfOneCorrectAnswer() {
        Answer correctAnswer = mock(Answer.class);
        when(correctAnswer.getIsCorrect()).thenReturn(true);

        List<Answer> answersOfP1 = mock(List.class);
        when(answersOfP1.size()).thenReturn(1);
        when(answersOfP1.get(0)).thenReturn(correctAnswer);
        return answersOfP1;
    }

    private List<Answer> givenMockedListOfNoCorrectAnswers() {
        return mock(List.class);
    }

}
