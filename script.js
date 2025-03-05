let currentQuestionIndex = 0;
let questions = [];

async function fetchQuestions() {
    const response = await fetch('http://localhost:8080/QuizApp/QuizServlet');
    questions = await response.json();
    displayQuestion();
}

function displayQuestion() {
    const questionElement = document.getElementById('question');
    const optionsContainer = document.getElementById('options');
    
    if (currentQuestionIndex >= questions.length) {
        questionElement.innerText = "Quiz Over!";
        optionsContainer.innerHTML = "";
        return;
    }
    
    const currentQuestion = questions[currentQuestionIndex];
    questionElement.innerText = currentQuestion.question;
    
    optionsContainer.innerHTML = "";
    currentQuestion.options.forEach((option, index) => {
        const button = document.createElement("button");
        button.innerText = option;
        button.onclick = () => nextQuestion();
        optionsContainer.appendChild(button);
    });
}

function nextQuestion() {
    currentQuestionIndex++;
    displayQuestion();
}
fetchQuestions();